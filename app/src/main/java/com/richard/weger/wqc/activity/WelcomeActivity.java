package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.Strings;
import com.google.zxing.Result;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.domain.dto.FileDTO;
import com.richard.weger.wqc.helper.ActivityHelper;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.DeviceRequestParameterResolver;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.service.ProjectRequestParametersResolver;
import com.richard.weger.wqc.service.QrScannerService;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.ErrorUtil;
import com.richard.weger.wqc.util.IMethod;
import com.richard.weger.wqc.util.LoggerManager;
import com.richard.weger.wqc.util.PermissionsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.richard.weger.wqc.appconstants.AppConstants.CAMERA_PERMISSION;
import static com.richard.weger.wqc.appconstants.AppConstants.EXTERNAL_DIR_PERMISSION;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_EDIT_SCREEN_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_ASKAUTHORIZATION_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_CONFIGLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_IDENTIFY_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_ITEMPICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PDFDOCUMENTSREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PDFREPORTDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTCREATE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;

public class WelcomeActivity extends Activity implements ZXingScannerView.ResultHandler,
        RestTemplateHelper.RestResponseHandler {

    Project project;
    String qrCode;
    List<EntityRestTemplateHelper> entityHelperQueue;
    List<FileRestTemplateHelper> fileHelperQueue;
    EntityRestTemplateHelper entityRestTemplateHelper;
    boolean hasAuthorization = false;
    TextView tvStatus;
    ParamConfigurations conf;
    QrScannerService qrService;

    String[] permissions = new String[]{
            CAMERA_PERMISSION,
            EXTERNAL_DIR_PERMISSION
    };

    @Override
    public void onPause(){
        super.onPause();
        if(qrService != null) {
            qrService.pause();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHelper.setWaitingLayout(this);
        new App();

        if(handlePermissions()){
            gotPermissions();
        }
    }

    @Override
    public void toggleControls(boolean bResume){
        runOnUiThread(() -> {
            try {
                (findViewById(R.id.buttonQrScan)).setEnabled(bResume);
                if (bResume) {
                    log("Enabling UI controls");
                    (findViewById(R.id.pbWelcome)).setVisibility(View.INVISIBLE);
                } else {
                    log("Disabling UI controls");
                    (findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored){}
        });
    }

    @Override
    public void onFatalError() {

    }

    private boolean handlePermissions(){
        log("Checking for user-app permissions");
        return PermissionsManager.checkPermission(permissions, this, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!PermissionsManager.checkPermission(permissions, this, false)){
            String message = App.getStringResource(R.string.permissionsNeededMessage);
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_PERMISSIONS_GRANT_WARNING, message, ErrorResult.ErrorLevel.SEVERE);
            ErrorResponseHandler.handle(err,  this::finish);
        } else {
            gotPermissions();
        }
    }

    public void gotPermissions(){
        log("User-app permissions granting successful");
        entityHelperQueue = new ArrayList<>();
        fileHelperQueue = new ArrayList<>();
        Configurations conf = ConfigurationsManager.getLocalConfig();
        if(conf.getServerPath() != null && conf.getServerPath().length() <= 0){
            firstContact();
        } else {
            ConfigurationsManager.loadServerConfig(this);
        }
    }

    @Override
    public void onBackPressed(){
        log("Back button pressed");
        if(hasAuthorization){
            log("Starting layout restore routine");
            layoutRestore();
        } else {
            log("Starting first run routine");
            firstContact();
        }
    }

    private void layoutRestore(){
        qrService = new QrScannerService(this);
        qrService.stop();
        log("Restoring layout to 'welcome' one");
        setContentView(R.layout.activity_welcome);
        setListeners();
        setTextEditValue(DeviceHelper.getCurrentDevice());
        tvStatus = findViewById(R.id.tvStatus);
    }

    public void QrScan(){
        log("Starting camera preview for qr scan");
        setContentView(qrService.getContentView());
    }

    private void setListeners(){
        log("Setting buttons listeners");

        findViewById(R.id.buttonQrScan).setOnClickListener(v -> QrScan());

        ImageButton button = findViewById(R.id.btnExit);
        button.setOnClickListener(v -> AlertHelper.showMessage(
                App.getStringResource(R.string.confirmationNeeded),
                App.getStringResource(R.string.closeQuestion),
                App.getStringResource(R.string.yesTAG),
                App.getStringResource(R.string.noTag),
                () -> android.os.Process.killProcess(android.os.Process.myPid()),
                null, this));
    }

    private void exit(){
        log("Started exit routine");
        if(entityRestTemplateHelper != null && entityRestTemplateHelper.getStatus() == AsyncTask.Status.RUNNING){
            entityRestTemplateHelper.cancel(true);
        }
        cancelPdfReportsGet();
        finish();
        super.onDestroy();
    }

    private void cancelPdfReportsGet(){
        log("Started routine to cancel http requests for pdf files");
        if(entityHelperQueue != null){
            for(EntityRestTemplateHelper r: entityHelperQueue){
                if(r != null && r.getStatus() == AsyncTask.Status.RUNNING){
                    r.cancel(true);
                }
            }
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        log("Started routine to handle qr scan result");
        qrService.pause();
        layoutRestore();
        handleQrScan(rawResult.getText());
    }

    protected void handleQrScan(String res){
        AbstractResult qrRes;
        qrCode = res;
        if(qrCode != null) {
            ProjectHelper.setConf(conf);
            qrRes = ProjectHelper.setQrCode(qrCode);
            if (qrRes instanceof ErrorResult) {
                ErrorResult err = ResultService.getErrorResult(qrRes);
                ErrorResponseHandler.handle(err, null);
                return;
            }
        } else {
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.QR_TRANSLATION_FAILED, App.getStringResource(R.string.invalidQrCodeString), ErrorResult.ErrorLevel.SEVERE);
            ErrorResponseHandler.handle(err, null);
            return;
        }
        ProjectHelper.projectLoad(this, false);
    }


    private void startProjectEdit(){
        log("Started routine to open the project edit screen");
        Intent intent = new Intent(getApplicationContext(), ProjectEditActivity.class);
        intent.putExtra(PROJECT_KEY, project);
        startActivityForResult(intent, PROJECT_EDIT_SCREEN_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PROJECT_EDIT_SCREEN_KEY) {
            log("Close app request received from the user");
            exit();
        }
    }

    private void getExistingItemPictures(){
        tvStatus.setText(R.string.retrievingReportPicturesTag);
        ProjectHelper.getItemPicturesList(this);
    }

    private void getExistingPdfDocuments(){
        tvStatus.setText(R.string.retrievingPdfsTag);
        ProjectHelper.getPdfsList(this);
    }

    private void getExistingGenPictures(){
        tvStatus.setText(R.string.retrievingGeneralPicturesTag);
        ProjectHelper.getGenPicturesList(this);
    }

    private void setTextEditValue(Device device){
        TextView tv = findViewById(R.id.tvDeviceId);
        tv.setText(
                device.getName()
                        .concat(" - ")
                        .concat(device.getRoles().stream().map(Role::getDescription).collect(Collectors.joining(", ")))
                        .concat(" (")
                        .concat(device.getDeviceid())
                        .concat(")")
        );
        tv = findViewById(R.id.tvVersion);
        tv.setText(String.format("v%s", App.getExpectedVersion()));
    }

    private void requestServerAuthorization(){
        log("Started authorization request routine");

        DeviceRequestParameterResolver resolver = new DeviceRequestParameterResolver(REST_ASKAUTHORIZATION_KEY, conf, true);
        resolver.postEntity(new Device(), this);
    }

    private void serverPathReferenceUpdate(String path){
        Configurations conf = ConfigurationsManager.getLocalConfig();
        conf.setServerPath(path);
        ConfigurationsManager.setLocalConfig(conf);
    }

    private void firstContact(){
        log("Configuration file not found, starting first run routine");
        String prevPath;
        prevPath = ConfigurationsManager.getLocalConfig().getServerPath();
        AlertHelper.getString(App.getStringResource(R.string.serverPathRequestMessage),
            (path) -> {
                if(path == null){
                    finish();
                } else {
                    serverPathReferenceUpdate(path);
                    ConfigurationsManager.loadServerConfig(this);
                }
            }, prevPath);
    }

    private void identify(){
        log("Started identify request routine");
        DeviceRequestParameterResolver resolver = new DeviceRequestParameterResolver(REST_IDENTIFY_KEY, conf, true);
        resolver.getEntity(new Device(), this);
    }

    private void identificationResponse(Device device){
        try {
            log("Starting local local configuration update");
            Configurations conf = ConfigurationsManager.getLocalConfig();
            conf.setUserId(device.getId());
            conf.setUsername(device.getName());
            conf.setRoles(device.getRoles().stream().map(Role::getDescription).collect(Collectors.toList()));
            conf.setDeviceId(device.getDeviceid());

            if (!device.isEnabled()) {
                String message = App.getStringResource(R.string.accessDisabledMessage);
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_ACCESS_DISABLED_WARNING, message, ErrorResult.ErrorLevel.SEVERE);
                ErrorResponseHandler.handle(err,  this::finish);
            } else if (conf.getUsername() == null || conf.getUsername().equals("")) {
                String message = App.getStringResource(R.string.invalidUsernameMessage);
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_INVALID_USERNAME_WARNING, message, ErrorResult.ErrorLevel.SEVERE);
                ErrorResponseHandler.handle(err,  this::finish);
            } else if (conf.getRoles() == null || conf.getRoles().size() == 0 ){
                String message = App.getStringResource(R.string.noRolesMessage);
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_EMPTY_ROLES_LIST_WARNING, message, ErrorResult.ErrorLevel.SEVERE);
                ErrorResponseHandler.handle(err,  this::finish);
            } else {
                ConfigurationsManager.setLocalConfig(conf);
                log("Device authorized");
                hasAuthorization = true;
                layoutRestore();
//                if(BuildConfig.DEBUG){
//                    handleQrScan("\\17-1-435_Z_1_T_1");
//                }
            }
        } catch (Exception ex) {
            String message = App.getStringResource(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")");
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_DEVICE_NOT_AUTHORIZED_EXCEPTION, message, ErrorResult.ErrorLevel.SEVERE);
            ErrorResponseHandler.handle(err,  this::finish);
            severe(ex.getMessage());
        }
    }

    private void log(String message){
        LoggerManager.log(WelcomeActivity.class, message, ErrorResult.ErrorLevel.LOG);
    }

    private void severe(String message){
        LoggerManager.log(WelcomeActivity.class, message, ErrorResult.ErrorLevel.SEVERE);
    }

    private void warning(String message){
        LoggerManager.log(WelcomeActivity.class, message, ErrorResult.ErrorLevel.WARNING);
    }

    private void log(ErrorResult err){
        LoggerManager.log(WelcomeActivity.class, err);
    }

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        if (result instanceof SuccessResult){
            switch (result.getRequestCode()) {
                case REST_CONFIGLOAD_KEY:
                    log("The response was a configuration object");
                    ParamConfigurations c = ResultService.getSingleResult(result, ParamConfigurations.class);
                    ConfigurationsManager.setServerConfig(c);
                    conf = c;
                    identify();
                    break;
                case REST_IDENTIFY_KEY:
                    log("The response was a result from a identify request");
                    Device device = ResultService.getSingleResult(result, Device.class);
                    identificationResponse(device);
                    break;
                case REST_QRPROJECTCREATE_KEY:
                    ProjectHelper.projectLoad(this, false);
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    toggleControls(false);
                    project = ResultService.getSingleResult(result, Project.class);
                    getExistingPdfDocuments();
                    break;
                case REST_PDFDOCUMENTSREQUEST_KEY:
                    LoggerManager.getLogger(WelcomeActivity.class).info("Got response from pdf list request");
                    List<FileDTO> pdfDocuments = ResultService.getMultipleResult(result, FileDTO.class);
                    List<String> toDownload = ProjectHelper.getObsoletePdfDocuments(pdfDocuments, project);
                    if(toDownload.size() > 0) {
                        LoggerManager.getLogger(WelcomeActivity.class).info("Outdated pdf files exists");
                        fileHelperQueue.clear();
                        String text = App.getStringResource(R.string.retrievingPdfsTag)
                                .concat(" - ")
                                .concat(App.getContext().getResources().getString(R.string.remainingTag, toDownload.size()));
                        tvStatus.setText(text);
                        ProjectHelper.getPdfDocuments(toDownload, fileHelperQueue, this);
                        return;
                    } else {
                        LoggerManager.getLogger(WelcomeActivity.class).info("No outdated pdf files were found");
                    }
                    getExistingItemPictures();
                    break;
                case REST_ITEMPICTURESREQUEST_KEY:
                    LoggerManager.getLogger(WelcomeActivity.class).info("Got response from item pictures list request");
                    List<FileDTO> itemPictures = ResultService.getMultipleResult(result, FileDTO.class);
                    toDownload = ProjectHelper.getObsoleteItemPictures(itemPictures, project);
                    if(toDownload.size() > 0) {
                        LoggerManager.getLogger(WelcomeActivity.class).info("Outdated item pictures exists");
                        fileHelperQueue.clear();
                        String text = App.getStringResource(R.string.retrievingReportPicturesTag)
                                .concat(" - ")
                                .concat(App.getContext().getResources().getString(R.string.remainingTag, toDownload.size()));
                        tvStatus.setText(text);
                        ProjectHelper.getItemPictures(toDownload, fileHelperQueue, this);
                        return;
                    } else {
                        LoggerManager.getLogger(WelcomeActivity.class).info("No outdated pictures were found");
                    }
                    getExistingGenPictures();
                    break;
                case REST_GENPICTURESREQUEST_KEY:
                    LoggerManager.getLogger(WelcomeActivity.class).info("Got response from general pictures list request");
                    List<FileDTO> pictures = ResultService.getMultipleResult(result, FileDTO.class);
                    toDownload = ProjectHelper.getObsoleteGenPictures( pictures, project);
                    if(toDownload.size() > 0) {
                        LoggerManager.getLogger(WelcomeActivity.class).info("Outdated general pictures exists");
                        String text = App.getStringResource(R.string.retrievingGeneralPicturesTag)
                                .concat(" - ")
                                .concat(App.getContext().getResources().getString(R.string.remainingTag, pictures.size()));
                        tvStatus.setText(text);
                        ProjectHelper.getGenPictures(toDownload, fileHelperQueue, this);
                        return;
                    } else {
                        LoggerManager.getLogger(WelcomeActivity.class).info("No outdated general pictures were found");
                    }
                    startProjectEdit();
                    break;
                case REST_PDFREPORTDOWNLOAD_KEY:
                    LoggerManager.getLogger(WelcomeActivity.class).info("Pdf file received");
                    continueIfPossible(App.getStringResource(R.string.retrievingPdfsTag), this::getExistingItemPictures);
                    break;
                case REST_PICTUREDOWNLOAD_KEY:
                    LoggerManager.getLogger(WelcomeActivity.class).info("Item picture received");
                    continueIfPossible(App.getStringResource(R.string.retrievingReportPicturesTag), this::getExistingGenPictures);
                    break;
                case REST_GENPICTUREDOWNLOAD_KEY:
                    LoggerManager.getLogger(WelcomeActivity.class).info("General picture received");
                    continueIfPossible(App.getStringResource(R.string.retrievingGeneralPicturesTag), this::startProjectEdit);
                    break;
            }
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            if (!Strings.isEmptyOrWhitespace(err.getDescription())) {
                log(err);
            } else {
                warning("An unexpected error has occurred while trying to retrieve data from server with request code '" + err.getRequestCode() + "'");
            }

            if(err.getCode() != null && err.getCode().equals(ErrorResult.ErrorCode.INVALID_APP_VERSION.toString())){
                ErrorResponseHandler.handle(err,this::finish);
                return;
            }

            String message;
            ErrorResult err2;
            switch (result.getRequestCode()) {
                case REST_CONFIGLOAD_KEY:
                    message = App.getStringResource(R.string.serverConnectErrorMessage);
                    err2 = new ErrorResult(ErrorResult.ErrorCode.CLIENT_SERVER_CONNECTION_FAILED_WARNING, message, ErrorResult.ErrorLevel.SEVERE);
                    ErrorResponseHandler.handle(err2, App.getStringResource(R.string.yesTAG), App.getStringResource(R.string.noTag), this::firstContact, this::finish);
                    break;
                case REST_IDENTIFY_KEY:
                    requestServerAuthorization();
                    message = App.getStringResource(R.string.deviceNotAuthorizedMessage).concat("\n(");
                    message = message.concat(App.getUniqueId()).concat(")");
                    err2  = new ErrorResult(ErrorResult.ErrorCode.CLIENT_DEVICE_NOT_AUTHORIZED_EXCEPTION, message, ErrorResult.ErrorLevel.SEVERE);
                    ErrorResponseHandler.handle(err2, this::finish);
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    if(err.getCode() != null && err.getCode().equals(ErrorResult.ErrorCode.ENTITY_NOT_FOUND.toString())) {
                        ProjectRequestParametersResolver resolver = new ProjectRequestParametersResolver(REST_QRPROJECTCREATE_KEY, conf, true);
                        resolver.postEntity(ProjectHelper.getProject(qrCode), this);
                    } else {
                        ErrorResponseHandler.handle(err, () -> toggleControls(true));
                    }
                    break;
                case REST_QRPROJECTCREATE_KEY:
                    message = App.getStringResource(R.string.projectCreationRequestFailed).concat("\n").concat(App.getStringResource(R.string.tryAgainLaterMessage));
                    err2  = new ErrorResult(ErrorResult.ErrorCode.CLIENT_PROJECT_CREATION_REQUEST_EXCEPTION, message, ErrorResult.ErrorLevel.SEVERE);
                    ErrorResponseHandler.handle(err2, null);
                    break;
                case REST_PDFREPORTDOWNLOAD_KEY:
                    continueIfPossible(App.getStringResource(R.string.retrievingPdfsTag), this::getExistingItemPictures);
                    break;
                case REST_PICTUREDOWNLOAD_KEY:
                    continueIfPossible(App.getStringResource(R.string.retrievingReportPicturesTag), this::getExistingGenPictures);
                    break;
                case REST_GENPICTUREDOWNLOAD_KEY:
                    continueIfPossible(App.getStringResource(R.string.retrievingGeneralPicturesTag), this::startProjectEdit);
                    break;
                default:
                    AlertHelper.showMessage(
                            ErrorUtil.getErrorMessageWithCode(err),
                            App.getStringResource(R.string.okTag),
                            null);
                    break;
            }
            if(!ProjectHelper.hasPendingTasks(entityHelperQueue, fileHelperQueue, true)){
                toggleControls(true);
            }
        }
    }

    private void continueIfPossible(String message, IMethod nextChain){
        if (message == null){
            message = App.getStringResource(R.string.retrievingFilesTag);
        }
        if (!ProjectHelper.hasPendingTasks(entityHelperQueue, fileHelperQueue, true)) {
            if(nextChain != null) {
                nextChain.execute();
            } else {
                toggleControls(true);
            }
        } else {
            int remaining;
            remaining = fileHelperQueue.size() - 1;
            String text = message.concat(" - ")
                    .concat(App.getContext().getResources().getString(R.string.remainingTag, remaining));
            runOnUiThread(() -> tvStatus.setText(text));
        }
    }

}
