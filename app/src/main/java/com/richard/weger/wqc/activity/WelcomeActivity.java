package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.util.DeviceManager;
import com.richard.weger.wqc.helper.JsonHelper;
import com.richard.weger.wqc.util.PermissionsManager;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.ReportHelper;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.richard.weger.wqc.constants.AppConstants.*;
import static com.richard.weger.wqc.helper.LogHelper.writeData;

public class WelcomeActivity extends Activity implements ZXingScannerView.ResultHandler, RestTemplateHelper.RestHelperResponse {

    Project project;
    String qrCode;
    List<RestTemplateHelper> restTemplateHelperQueue;
    RestTemplateHelper restTemplateHelper;
    boolean hasAuthorization = false;

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
        ((findViewById(R.id.btnExit))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                onDestroy();
            }
        });

        writeData("Starting permissions handler");
        if(handlePermissions()){
            writeData("User-app permissions granting successful");
            gotPermissions();
        }

        FirebaseMessaging.getInstance().subscribeToTopic("wqc-2.0");
    }

    private void toggleControls(boolean bResume){

        (findViewById(R.id.buttonQrScan)).setEnabled(bResume);
        (findViewById(R.id.btnConfig)).setEnabled(bResume);
        if(bResume) {
            writeData("Enabling UI controls");
            (findViewById(R.id.pbWelcome)).setVisibility(View.INVISIBLE);
        }
        else {
            writeData("Disabling UI controls");
            (findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
        }
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
            builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        } else {
            gotPermissions();
        }
    }

    private void gotPermissions(){
        restTemplateHelperQueue = new ArrayList<>();
        writeData("Loading local configurations");
        Configurations conf = ConfigurationsManager.getLocalConfig();
        if(conf.getServerPath().length() <= 0){
            writeData("Configuration file not found");
            writeData("Starting first run routine");
            firstContact();
        } else {
            writeData("Configuration file found. Starting identification routine");
            identify();
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
        setTextEditValue(DeviceManager.getCurrentDevice());
    }

    public void QrScan(){
        writeData("Starting camera preview for qr scan");
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        writeData("Started camera preview for qr scan");
    }

    private void setListeners(){
        writeData("Setting buttons listeners");
        Button button = findViewById(R.id.btnConfig);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this,
                        ConfigurationsActivity.class);
                startActivityForResult(intent, CONFIG_SCREEN_KEY);
            }
        });

        findViewById(R.id.buttonQrScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                qrCode = "17-1-435_Z_1_T_1";
//                layoutRestore();
//                startDataLoad();
                QrScan();
            }
        });

        button = findViewById(R.id.btnExit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                builder.setTitle(R.string.confirmationNeeded);
                builder.setMessage(R.string.closeMessage);
                builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        });
        writeData("Buttons listeners set finished");
    }

    private void exit(){
        writeData("Started exit routine");
        if(restTemplateHelper != null && restTemplateHelper.getStatus() == AsyncTask.Status.RUNNING){
            restTemplateHelper.cancel(true);
        }
        cancelPdfReportsGet();
        writeData("Application finished");
        finish();
        super.onDestroy();
    }

    private void cancelPdfReportsGet(){
        writeData("Started routine to cancel http requests for pdf files");
        if(restTemplateHelperQueue != null){
            for(RestTemplateHelper r: restTemplateHelperQueue){
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
        startDataLoad();
    }

    private void startDataLoad(){
        writeData("Started routine to get the configs from server");
        restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_CONFIGLOAD_KEY);
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
    }

    private void projectLoad(){
        writeData("Started routine to get the project from server");
        restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_QRPROJECTLOAD_KEY);
        uriBuilder.getParameters().add(qrCode);
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
    }

    private void startProjectEdit(){
        writeData("Started routine to open the project edit screen");
        Intent intent = new Intent(WelcomeActivity.this, ProjectEditActivity.class);
        intent.putExtra(PROJECT_KEY, project);
        startActivityForResult(intent, PROJECT_EDIT_SCREEN_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        writeData("Started routine to handle activity's result");
        if(requestCode == PROJECT_EDIT_SCREEN_KEY) {
            if (requestCode == RESULT_CANCELED) {
                ProjectHelper.reportFilesErase(project);
                startDataLoad();
            } else {
                writeData("Close app request received from the user");
                exit();
            }
        }
    }

    private boolean needsPictures(boolean request){
        writeData("Started routine to check if pictures download is necessary");
        List<Item> items = ProjectHelper.itemsWithMissingPictures(project);
        if(items.size() == 0){
            writeData("No pictures download is needed");
            return false;
        } else {
            if(request) {
                writeData("Pictures missing. Started routine to download them from the server");
                (new ReportHelper()).requestPictures(this, items, project);
//                for (Item item : missingPictures){
//                    (new ReportHelper()).getPictures(this, missingPictures, restTemplateHelperQueue);
//                }
            }
            return true;
        }
    }

    private boolean needsPdfFiles(boolean request){
        writeData("Started routine to check if pdf files download is necessary");
        if(ProjectHelper.hasAllReportFiles(project)) {
            writeData("No pdf files are needed at all");
            return false;
        } else {
            if(request) {
                writeData("Pdf files missing. Started routine to download them from the server");
                for (Report r : project.getDrawingRefs().get(0).getReports()) {
                    if (r instanceof CheckReport) {
                        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
                        restTemplateHelperQueue.add(restTemplateHelper);
                        (new ReportHelper()).getPdfFilesPath((CheckReport) r, restTemplateHelper);
                    }
                }
            }
            return true;
        }
    }

    private void dataLoadError(String customMessage, String buttons, final boolean finish, final boolean justIdentityAgain) {
        writeData("Started routine to show error message to the user");
        try {
            toggleControls(true);
            cancelPdfReportsGet();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message;
        if(finish){
            builder.setCancelable(false);
        }
        if (customMessage != null && !customMessage.equals("")) {
            message = customMessage;
        } else {
            if(!finish) {
                message = getResources().getString(R.string.unknownErrorMessage)
                        .concat(". ")
                        .concat(getResources().getString(R.string.tryAgainMessage));
            } else {
                message = getResources().getString(R.string.unknownErrorMessage);
            }
        }
        builder.setMessage(message);
        builder.setPositiveButton((buttons.equals(YES_NO) ? R.string.yesTAG : R.string.okTag), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(finish){
                    exit();
                } else {
                    if (justIdentityAgain) {
                        identify();
                    } else {
                        firstContact();
                    }
                }
            }
        });
        if (buttons.equals(YES_NO)){
            if(!finish) {
                builder.setNegativeButton(R.string.noTag, null);
            } else {
                builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }
        }
        builder.show();
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        boolean needsPdfFiles,
                needsPictures;
        writeData("Started routine to handle the http response got from the server");
        if (result != null) {
            if (!result.equals(App.getContext().getResources().getString(R.string.drawingLockedMessage))) {
                switch (requestCode) {
                    case REST_CONFIGLOAD_KEY:
                        writeData("The response was a configuration object");
                        ConfigurationsManager.setServerConfig(JsonHelper.toObject(result, ParamConfigurations.class));
                        projectLoad();
                        break;
                    case REST_QRPROJECTLOAD_KEY:
                        writeData("The response was a project");
                        if (!result.equals("")) {
                            project = ProjectHelper.fromJson(result);
                            if (project == null) {
                                writeData("Invalid project got from the server");
                                dataLoadError(null, YES_NO, false, true);
                                return;
                            }
                            needsPdfFiles = needsPdfFiles(true);
                            needsPictures = needsPictures(true);
                            if (!(needsPdfFiles || needsPictures)) {
                                writeData("Starting routine to open the project edit screen");
//                                ProjectHelper.linkExistingPictures(project);
                                startProjectEdit();
                            }
                        }
                        break;
                    case REST_PDFREPORTREQUEST_KEY:
                        writeData("The response was a Pdf file");
                        if(!ReportHelper.hasPendingTasks(restTemplateHelperQueue, true)){
                            startProjectEdit();
                        }
                        break;
                    case REST_PICTURESREQUEST_KEY:
                        writeData("Got existing pictures list from server");
                        List<Item> items = JsonHelper.toList(result, Item.class);
                        if(items.size() > 0) {
                            for(int i = 0; i < items.size(); i++){
                                Item item = items.get(i);
                                items.set(i, ProjectHelper.getItemReferences(project, item));
                            }
                            ReportHelper reportHelper = new ReportHelper();
                            reportHelper.getPictures(this, items, restTemplateHelperQueue);
                        } else {
                            startProjectEdit();
                        }
                        break;
                    case REST_PICTUREDOWNLOAD_KEY:
                        writeData("The response was a JPG file");
                        if (!ReportHelper.hasPendingTasks(restTemplateHelperQueue, true)) {
                            startProjectEdit();
                        }
                        break;
                    case REST_PROJECTSAVE_KEY:
                        writeData("The response was a result from a project save request");
                        startProjectEdit();
                        break;
                    case REST_FIRSTCONNECTIONTEST_KEY:
                        writeData("The response was a result from a first connection request");
                        if (result.toLowerCase().equals("success")) {
                            identify();
                        } else {
                            dataLoadError(getResources().getString(R.string.serverConnectErrorMessage), YES_NO, false, false);
                        }
                        break;
                    case REST_IDENTIFY_KEY:
                        writeData("The response was a result from a identify request");
                        Device device;
                        try {
                            writeData("Trying to parse result to device entity");
                            device = JsonHelper.toObject(result, Device.class);
                            writeData("Parse successful");

                            writeData("Starting local local configuration update");
                            Configurations conf = ConfigurationsManager.getLocalConfig();
                            conf.setUserId(device.getId());
                            conf.setUsername(device.getName());
                            conf.setRole(device.getRole());
                            conf.setDeviceId(device.getDeviceid());

                            if (conf.getRole() == null || conf.getUsername() == null || !device.isEnabled()
                                    || conf.getRole().equals("") || conf.getUsername().equals("")) {
                                writeData("No authorization found for this device - based on the retrieved configuration");
//                            requestServerAuthorization();
                                dataLoadError(getResources().getString(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")"), OK, true, true);
                            } else {
                                writeData("Updating local configuration");
                                ConfigurationsManager.setLocalConfig(conf);
                                writeData("Device authorized");
                                hasAuthorization = true;
                                layoutRestore();
                            }
                        } catch (Exception ex) {
                            writeData("General error when trying to retrieve an authorization for this device - based on the retrieved configuration");
                            ex.printStackTrace();
                            writeData("Starting authorization request routine");
                            requestServerAuthorization();
                            dataLoadError(getResources().getString(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")"), OK, true, true);
                        }
                        break;
                }
            } else {
                writeData("A write operation request was made but the current drawing is locked by another user. Aborting write operation.");
                toggleControls(false);
                projectLoad();
                dataLoadError(App.getContext().getResources().getString(R.string.drawingLockedMessage), OK, false, true);
            }
        } else {
            switch (requestCode) {
                case REST_FIRSTCONNECTIONTEST_KEY:
                    writeData("A first connection request was made but no valid response was received. Maybe the server is in shutdown state, inaccessible or the informed ip address is wrong");
                    dataLoadError(getResources().getString(R.string.serverConnectErrorMessage), YES_NO, false, false);
                    break;
                case REST_IDENTIFY_KEY:
                    writeData("An identify request was made but an empty response was received. Maybe this device is not yet authorized to use the app.");
                    requestServerAuthorization();
                    dataLoadError(getResources().getString(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")"), OK, true, true);
                    break;
                default:
                    if (!hasAuthorization) {
                        writeData("Empty response was received");
                        dataLoadError(null, OK, true, true);
                    } else {
                        writeData("Empty response was received");
                        dataLoadError(null, YES_NO, false, true);
                    }
                    break;
            }
        }
    }

    private void setTextEditValue(Device device){
        TextView tv = findViewById(R.id.tvDeviceId);
        tv.setText(
                device.getName()
                        .concat(" - ")
                        .concat(device.getRole())
                        .concat(" (")
                        .concat(device.getDeviceid())
                        .concat(")")
        );
    }

    private void requestServerAuthorization(){
        writeData("Started authorization request routine");
        Device device = new Device();
        device.setDeviceid(App.getUniqueId());
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_ASKAUTHORIZATION_KEY);
        uriBuilder.setDevice(device);
        restTemplateHelper.execute(uriBuilder);
    }

    private void firstContact(){
        writeData("Started first contact request routine");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.serverPathRequestMessage));
        final EditText input = new EditText(this);
//        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.okTag),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restTemplateHelper = new RestTemplateHelper(WelcomeActivity.this);
                        Configurations conf = new Configurations();
                        conf.setServerPath(input.getText().toString());
                        ConfigurationsManager.setLocalConfig(conf);
                        UriBuilder uriBuilder = new UriBuilder();
                        uriBuilder.setRequestCode(REST_FIRSTCONNECTIONTEST_KEY);
                        restTemplateHelper.execute(uriBuilder);
                    }
                });
        builder.show();
    }

    private void identify(){
        writeData("Started identify request routine");
        List<String> parameters = new ArrayList<>();
        parameters.add(App.getUniqueId());
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_IDENTIFY_KEY);
        uriBuilder.setParameters(parameters);
        restTemplateHelper = new RestTemplateHelper(this);
        restTemplateHelper.execute(uriBuilder);
    }

    /*
    private void authenticate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.passwordProtectedTitle));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.okTag),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Configurations config;
                config = ConfigurationsManager.getLocalConfig(WelcomeActivity.this);
                String password;
                password = input.getText().toString();
                if(config.getAppPassword().equals(password)){
                    Intent intent = new Intent(WelcomeActivity.this,
                            ConfigurationsActivity.class);
                    startActivityForResult(intent, CONFIG_SCREEN_KEY);
                }
                else{
                    Toast.makeText(WelcomeActivity.this,
                            R.string.invalidPasswordMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();
    }
    */
}
