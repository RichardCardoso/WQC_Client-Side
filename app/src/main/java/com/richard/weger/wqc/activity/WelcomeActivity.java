package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.Result;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.helper.MessageboxHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.entity.EntityRestResult;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileRestResult;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.service.DeviceRequestParameterResolver;
import com.richard.weger.wqc.service.FileRequestParametersResolver;
import com.richard.weger.wqc.service.ParamConfigurationsRequestParametersResolver;
import com.richard.weger.wqc.service.ProjectRequestParametersResolver;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.util.PermissionsManager;
import com.richard.weger.wqc.helper.ProjectHelper;

import org.springframework.http.HttpStatus;

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
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PDFREPORTDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PROJECTSAVE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTCREATE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;
import static com.richard.weger.wqc.helper.LogHelper.writeData;

public class WelcomeActivity extends Activity implements ZXingScannerView.ResultHandler,
        EntityRestTemplateHelper.EntityRestResponse,
        FileRestTemplateHelper.FileRestResponse {

    Project project;
    String qrCode;
    List<EntityRestTemplateHelper> entityHelperQueue;
    List<FileRestTemplateHelper> fileHelperQueue;
    EntityRestTemplateHelper entityRestTemplateHelper;
    boolean hasAuthorization = false;
    boolean checkedForGenPictures = false;
    boolean projectEditStarted = false;
    TextView tvStatus;
    ParamConfigurations conf;

    private ZXingScannerView mScannerView;
    PermissionsManager permissionsManager = new PermissionsManager();
    String[] permissions = new String[]{
            CAMERA_PERMISSION,
            EXTERNAL_DIR_PERMISSION
    };

    @Override
    public void onPause(){
        super.onPause();
        if(mScannerView != null)
            mScannerView.stopCamera();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new App();

        setContentView(R.layout.activity_wait);
        (findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
        ((findViewById(R.id.btnExit))).setOnClickListener(v -> {
            finish();
            onDestroy();
        });

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
                    writeData("Enabling UI controls");
                    (findViewById(R.id.pbWelcome)).setVisibility(View.INVISIBLE);
                } else {
                    writeData("Disabling UI controls");
                    (findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored){}
        });
    }

    @Override
    public void onError() {
        checkedForGenPictures = false;

    }

    private boolean handlePermissions(){
        writeData("Checking for user-app permissions");
        return permissionsManager.checkPermission(permissions, this, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!permissionsManager.checkPermission(permissions, this, false)){
            writeData("Failed to get all the needed permissions");
            AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
            builder.setTitle(R.string.noPermissionsGrantedTitle);
            builder.setMessage(R.string.permissionsNeededMessage);
            builder.setPositiveButton(R.string.okTag, (dialog, which) -> finish());
            builder.show();
        } else {
            gotPermissions();
        }
    }

    private void gotPermissions(){
        writeData("User-app permissions granting successful");
        entityHelperQueue = new ArrayList<>();
        fileHelperQueue = new ArrayList<>();
        Configurations conf = ConfigurationsManager.getLocalConfig();
        if(conf.getServerPath() != null && conf.getServerPath().length() <= 0){
            firstContact();
        } else {
            getServerConfig();
        }
    }

    @Override
    public void onBackPressed(){
        writeData("Back button pressed");
        if(hasAuthorization){
            writeData("Starting layout restore routine");
            layoutRestore();
        } else {
            writeData("Starting first run routine");
            firstContact();
        }
    }

    private void layoutRestore(){
        if(mScannerView != null) {
            writeData("Stopping camera's qr scan");
            mScannerView.stopCamera();
            mScannerView = null;
        }
        writeData("Restoring layout to 'welcome' one");
        setContentView(R.layout.activity_welcome);
        setListeners();
        setTextEditValue(DeviceHelper.getCurrentDevice());
         tvStatus = findViewById(R.id.tvStatus);
    }

    public void QrScan(){
        writeData("Starting camera preview for qr scan");
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    private void setListeners(){
        writeData("Setting buttons listeners");

        findViewById(R.id.buttonQrScan).setOnClickListener(v -> QrScan());

        Button button = findViewById(R.id.btnExit);
        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
            builder.setTitle(R.string.confirmationNeeded);
            builder.setMessage(R.string.closeQuestion);
            builder.setPositiveButton(R.string.yesTAG, (dialogInterface, i) -> finish());
            builder.setNegativeButton(R.string.noTag, (dialogInterface, i) -> {});
            builder.show();
        });
    }

    private void exit(){
        writeData("Started exit routine");
        if(entityRestTemplateHelper != null && entityRestTemplateHelper.getStatus() == AsyncTask.Status.RUNNING){
            entityRestTemplateHelper.cancel(true);
        }
        cancelPdfReportsGet();
        finish();
        super.onDestroy();
    }

    private void cancelPdfReportsGet(){
        writeData("Started routine to cancel http requests for pdf files");
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
        writeData("Started routine to handle qr scan result");
        qrCode = rawResult.getText();
        if(mScannerView != null) {
            mScannerView.stopCamera();
        }
        layoutRestore();
        projectLoad();
    }

    private void getServerConfig(){
        writeData("Started routine to get the configs from server");
        ParamConfigurationsRequestParametersResolver resolver = new ParamConfigurationsRequestParametersResolver(REST_CONFIGLOAD_KEY, null, true);
        resolver.getEntity(new ParamConfigurations(), this);
    }

    private void projectLoad(){
        toggleControls(false);
        writeData("Started routine to get the project from server");
        ProjectRequestParametersResolver resolver = new ProjectRequestParametersResolver(REST_QRPROJECTLOAD_KEY, conf, true);
        resolver.getEntity(ProjectHelper.getProject(qrCode, conf), this);
    }

    private void startProjectEdit(){
        if(!projectEditStarted) {
            FirebaseMessaging.getInstance().subscribeToTopic("wqc-2.0");
            writeData("Started routine to open the project edit screen");
            Intent intent = new Intent(WelcomeActivity.this, ProjectEditActivity.class);
            intent.putExtra(PROJECT_KEY, project);
            startActivityForResult(intent, PROJECT_EDIT_SCREEN_KEY);
            projectEditStarted = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PROJECT_EDIT_SCREEN_KEY) {
            projectEditStarted = false;
            writeData("Close app request received from the user");
            exit();
        }
    }

    private boolean requestPicturesIfNeeded(){
        tvStatus.setText(R.string.retrievingReportPicturesTag);
        List<Item> items = ProjectHelper.itemsWithMissingPictures(project, true);
        if(items.size() == 0){
            writeData("No pictures download is needed");
            return false;
        } else {
            writeData("Pictures missing. Started routine to download them from the server");
            for (Item item : items){
                FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_PICTUREDOWNLOAD_KEY, this);
                FileRestTemplateHelper helper = resolver.getPicture(item.getPicture().getFileName(), qrCode);
                fileHelperQueue.add(helper);
            }
            return true;
        }
    }

    private boolean requestPdfsIfNeeded(){
        tvStatus.setText(R.string.retrievingPdfsTag);
        if(ProjectHelper.validReportFilesCount(project) > 0) {
            writeData("No pdf files are needed at all");
            return false;
        } else {
            writeData("Pdf files missing. Started routine to download them from the server");
            for (Report _r : project.getDrawingRefs().get(0).getReports()) {
                if (_r instanceof CheckReport) {
                    CheckReport r = (CheckReport) _r;
                    FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_PDFREPORTDOWNLOAD_KEY, this);
                    FileRestTemplateHelper helper = resolver.getPdf(r, StringHelper.getQrText(project));
                    fileHelperQueue.add(helper);
                }
            }
            return true;
        }
    }

    private void checkForGenPictures(){
        tvStatus.setText(R.string.retrievingGeneralPicturesTag);
        if(!checkedForGenPictures) {
            ProjectHelper.getGenPicturesList(this, project, true);
            checkedForGenPictures = true;
        }
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
    }

    private void requestServerAuthorization(){
        writeData("Started authorization request routine");

        DeviceRequestParameterResolver resolver = new DeviceRequestParameterResolver(REST_ASKAUTHORIZATION_KEY, conf, true);
        resolver.postEntity(new Device(), this);
    }

    private void firstContact(){
        writeData("Configuration file not found, starting first run routine");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.serverPathRequestMessage));
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.okTag), (dialog, which) -> {
            String path = input.getText().toString();
            Configurations conf = ConfigurationsManager.getLocalConfig();
            conf.setServerPath(path);
            ConfigurationsManager.setLocalConfig(conf);
            getServerConfig();
        });
        builder.show();
    }

    private void identify(){
        writeData("Started identify request routine");
        DeviceRequestParameterResolver resolver = new DeviceRequestParameterResolver(REST_IDENTIFY_KEY, conf, true);
        resolver.getEntity(new Device(), this);
    }

    private <T extends DomainEntity> void identificationResponse(EntityRestResult<T> result){
        Device device;
        try {
            writeData("Trying to parse result to device entity");
            device = (Device) result.getEntities().get(0);

            writeData("Starting local local configuration update");
            Configurations conf = ConfigurationsManager.getLocalConfig();
            conf.setUserId(device.getId());
            conf.setUsername(device.getName());
            conf.setRoles(device.getRoles().stream().map(Role::getDescription).collect(Collectors.toList()));
            conf.setDeviceId(device.getDeviceid());

            if (!device.isEnabled()) {
                writeData("Access is disabled for this device - at the server user/device config");
                MessageboxHelper.showMessage(this,
                        getResources().getString(R.string.accessDisabledMessage),
                        getResources().getString(R.string.okTag),
                        this::finish);
            } else if (conf.getUsername() == null || conf.getUsername().equals("")) {
                writeData("Invalid device's username - at the server user/device config");
                MessageboxHelper.showMessage(this,
                        getResources().getString(R.string.invalidUsernameMessage),
                        getResources().getString(R.string.okTag),
                        this::finish);
            } else if (conf.getRoles() == null || conf.getRoles().size() == 0 ){
                writeData("Device's role list is empty - at the server user/device config");
                MessageboxHelper.showMessage(this,
                        getResources().getString(R.string.noRolesMessage),
                        getResources().getString(R.string.okTag),
                        this::finish);
            } else {
                ConfigurationsManager.setLocalConfig(conf);
                writeData("Device authorized");
                hasAuthorization = true;
                layoutRestore();
            }
        } catch (Exception ex) {
            writeData("General error when trying to retrieve an authorization for this device - based on the retrieved configuration");
            writeData(ex.getMessage());
            MessageboxHelper.showMessage(this,
                    getResources().getString(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")"),
                    getResources().getString(R.string.okTag),
                    this::finish);
        }
    }

    @Override
    public <T extends DomainEntity> void EntityRestCallback(EntityRestResult<T> result) {
        if ((result.getStatus() == HttpStatus.OK || result.getStatus() == HttpStatus.CREATED)){
            switch (result.getRequestCode()) {
                case REST_CONFIGLOAD_KEY:
                    writeData("The response was a configuration object");
                    ParamConfigurations c = (ParamConfigurations) result.getEntities().get(0);
                    ConfigurationsManager.setServerConfig(c);
                    conf = c;
                    identify();
                    break;
                case REST_IDENTIFY_KEY:
                    writeData("The response was a result from a identify request");
                    identificationResponse(result);
                    break;
                case REST_QRPROJECTCREATE_KEY:
                    projectLoad();
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    project = (Project) result.getEntities().get(0);
                    if(project == null){
                        MessageboxHelper.showMessage(this,
                                getResources().getString(R.string.unableToProcced),
                                getResources().getString(R.string.noDataTag),
                                getResources().getString(R.string.yesTAG),
                                getResources().getString(R.string.noTag),
                                null,null);
                        return;
                    }
                    new ProjectHelper(qrCode, conf);
                    boolean needsPdfFiles = requestPdfsIfNeeded();
                    boolean needsPictures = requestPicturesIfNeeded();
                    if (!(needsPdfFiles || needsPictures)) {
                        checkForGenPictures();
                    }
                    break;
                case REST_PROJECTSAVE_KEY:
                    writeData("The response was a result from a project save request");
                    startProjectEdit();
                    break;
            }
        } else {
            if(result.getMessage() != null && result.getMessage().length() > 0 ){
                writeData(result.getMessage());
            } else {
                writeData("An unexpected error has occurred while trying to retrieve data from server with request code '" + result.getRequestCode() + "'");
            }
            switch (result.getRequestCode()) {
                case REST_CONFIGLOAD_KEY:
                    MessageboxHelper.showMessage(this,
                            getResources().getString(R.string.unableToProcced),
                            getResources().getString(R.string.serverConnectErrorMessage),
                            getResources().getString(R.string.yesTAG),
                            getResources().getString(R.string.noTag),
                            this::firstContact,
                            null);
                    break;
                case REST_IDENTIFY_KEY:
                    writeData("An identify request was made but an empty response was received. Maybe this device is not yet authorized to use the app.");
                    requestServerAuthorization();
                    MessageboxHelper.showMessage(this,
                            getResources().getString(R.string.deviceNotAuthorizedMessage).concat(" (").concat(App.getUniqueId()).concat(")"),
                            getResources().getString(R.string.okTag),
                            this::finish);
                    break;
                case REST_QRPROJECTCREATE_KEY:
                    MessageboxHelper.showMessage(this,
                            getResources().getString(R.string.dataRecoverError),
                            getResources().getString(R.string.okTag),
                            null);
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    if(result.getStatus() == HttpStatus.NOT_FOUND){
                        writeData(result.getStatus().getReasonPhrase());
                        if(result.getMessage() != null){
                            writeData(result.getMessage());
                        }
                        ProjectRequestParametersResolver resolver = new ProjectRequestParametersResolver(REST_QRPROJECTCREATE_KEY, conf, true);
                        resolver.postEntity(ProjectHelper.getProject(qrCode, conf), this);
                    } else {
                        String msg = result.getStatus().getReasonPhrase();
                        if(result.getMessage() != null) {
                            msg = msg.concat(result.getMessage());
                        }
                        writeData(msg);
                        MessageboxHelper.showMessage(this,
                                getResources().getString(R.string.unknownErrorMessage),
                                getResources().getString(R.string.okTag),
                                null);
                        break;
                    }
                    break;
                default:
                    MessageboxHelper.showMessage(this,
                            getResources().getString(R.string.unknownErrorMessage),
                            getResources().getString(R.string.okTag),
                            null);
                    break;
            }
        }
    }

    @Override
    public void FileRestCallback(FileRestResult result) {
        if ((result.getStatus() == HttpStatus.OK || result.getStatus() == HttpStatus.CREATED)) {
            switch (result.getRequestCode()) {
                case REST_GENPICTURESREQUEST_KEY:
                    writeData("Got existing general pictures list from server");
                    List<String> pictures = result.getExistingContent();
                    if (pictures.size() > 0) {
                        fileHelperQueue.clear();
                        ProjectHelper.getGenPictures(this, pictures, fileHelperQueue, project);
                        String text = getResources().getString(R.string.retrievingGeneralPicturesTag)
                                .concat(" - ")
                                .concat(getResources().getString(R.string.remainingTag, pictures.size()));
                        tvStatus.setText(text);
                    } else {
                        startProjectEdit();
                    }
                    break;
                case REST_GENPICTUREDOWNLOAD_KEY:
                    writeData("The response was a JPG file");
                    continueIfPossible(getResources().getString(R.string.retrievingGeneralPicturesTag));
                    break;
                case REST_PDFREPORTDOWNLOAD_KEY:
                    writeData("The response was a Pdf file");
                    continueIfPossible(null);
                    break;
                case REST_PICTUREDOWNLOAD_KEY:
                    writeData("The response was a JPG file");
                    continueIfPossible(null);
                    break;
            }
        } else {
            switch(result.getRequestCode()){
                case REST_PDFREPORTDOWNLOAD_KEY:
                    writeData("A pdf file cannot be found or accessed at this time.");
                    continueIfPossible(null);
                    break;
                case REST_PICTUREDOWNLOAD_KEY:
                case REST_GENPICTUREDOWNLOAD_KEY:
                    writeData("A picture file cannot be found or accessed at this time.");
                    continueIfPossible(null);
                    break;
                default:
                    MessageboxHelper.showMessage(this,
                            getResources().getString(R.string.unknownErrorMessage),
                            getResources().getString(R.string.okTag),
                            null);
                    break;
            }
            if(!ProjectHelper.hasPendingTasks(entityHelperQueue, fileHelperQueue, true)){
                toggleControls(true);
            }
        }
    }

    private void continueIfPossible(String message){
        if (message == null){
            message = getResources().getString(R.string.retrievingFilesTag);
        }
        if (!ProjectHelper.hasPendingTasks(entityHelperQueue, fileHelperQueue, true)) {
            if(!checkedForGenPictures) {
                checkForGenPictures();
            } else {
                startProjectEdit();
            }
        } else {
            int remaining = 0;
            remaining += fileHelperQueue.size() - 1;
            String text = message.concat(" - ")
                    .concat(getResources().getString(R.string.remainingTag, remaining));
            runOnUiThread(() -> tvStatus.setText(text));
        }
    }

}
