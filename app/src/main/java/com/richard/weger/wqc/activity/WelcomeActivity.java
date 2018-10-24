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

import com.google.zxing.Result;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.util.DeviceManager;
import com.richard.weger.wqc.util.JsonHandler;
import com.richard.weger.wqc.util.PermissionsManager;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.ProjectHandler;
import com.richard.weger.wqc.util.ReportHelper;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.richard.weger.wqc.util.AppConstants.*;
import static com.richard.weger.wqc.util.LogHandler.writeData;

public class WelcomeActivity extends Activity implements ZXingScannerView.ResultHandler, RestTemplateHelper.HttpHelperResponse {

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

        writeData("Starting permissions handler", getExternalFilesDir(null));
        if(handlePermissions()){
            gotPermissions();
        }
    }

    private void toggleControls(boolean bResume){
        (findViewById(R.id.buttonQrScan)).setEnabled(bResume);
        (findViewById(R.id.btnConfig)).setEnabled(bResume);
        if(bResume) {
            (findViewById(R.id.pbWelcome)).setVisibility(View.INVISIBLE);
        }
        else {
            (findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
        }
    }

    private boolean handlePermissions(){
        return permissionsManager.checkPermission(permissions, this, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!permissionsManager.checkPermission(permissions, this, false)){
            writeData("Didn't got all the needed permissions", getExternalFilesDir(null));
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
        Configurations conf = ConfigurationsManager.getLocalConfig();
        if(conf.getServerPath().length() <= 0){
            firstContact();
        } else {
            identify();
        }
    }

    @Override
    public void onBackPressed(){
        if(hasAuthorization){
            layoutRestore();
        } else {
            firstContact();
        }
    }

    private void layoutRestore(){
        if(mScannerView != null) {
            mScannerView.stopCamera();
            mScannerView = null;
        }
        setContentView(R.layout.activity_welcome);
        setListeners();
        setTextEditValue(DeviceManager.getCurrentDevice());
    }

    public void QrScan(){
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    private void setListeners(){
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
                qrCode = "17-1-435_Z_1_T_1";
                layoutRestore();
                startDataLoad();
//                QrScan();
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
    }

    private void exit(){
        if(restTemplateHelper != null && restTemplateHelper.getStatus() == AsyncTask.Status.RUNNING){
            restTemplateHelper.cancel(true);
        }
        cancelPdfReportsGet();
        finish();
        super.onDestroy();
    }

    private void cancelPdfReportsGet(){
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
        qrCode = rawResult.getText();
        mScannerView.stopCamera();
        layoutRestore();
        startDataLoad();
    }

    private void startDataLoad(){
        restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_CONFIGLOAD_KEY);
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
    }

    private void projectLoad(){
        restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_QRPROJECTLOAD_KEY);
        uriBuilder.getParameters().add(qrCode);
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
    }

    private void startProjectEdit(){
        Intent intent = new Intent(WelcomeActivity.this, ProjectEditActivity.class);
        intent.putExtra(PROJECT_KEY, project);
        startActivityForResult(intent, PROJECT_EDIT_SCREEN_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PROJECT_EDIT_SCREEN_KEY) {
            if (requestCode == RESULT_CANCELED) {
                ProjectHandler.reportFilesErase(project);
                startDataLoad();
            } else {
                finish();
            }
        } else if (requestCode == CONFIG_SCREEN_KEY){

        }
    }

    private boolean needsPdfFiles(boolean request){
        if(ProjectHandler.hasAllReportFiles(project)) {
            return false;
        } else {
            if(request) {
                restTemplateHelperQueue = new ArrayList<>();
                for (Report r : project.getDrawingRefs().get(0).getReports()) {
                    if (r instanceof CheckReport) {
                        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
                        restTemplateHelperQueue.add(restTemplateHelper);
                        (new ReportHelper()).getPdfFilesPath(this, (CheckReport) r, restTemplateHelper);
                    }
                }
            }
            return true;
        }
    }

    private void dataLoadError(String customMessage, String buttons, final boolean finish, final boolean justIdentityAgain) {
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
        if (result != null) {
            if (!result.equals(App.getContext().getResources().getString(R.string.drawingLockedMessage))) {
                switch (requestCode) {
                    case REST_QRPROJECTLOAD_KEY:
                        if (!result.equals("")) {
                            project = JsonHandler.toProject(result);
                            if (project == null) {
                                dataLoadError(null, YES_NO, false, true);
                                return;
                            }
                            if (!needsPdfFiles(true)) {
                                startProjectEdit();
                            }
                        }
                        break;
                    case REST_CONFIGLOAD_KEY:
                        ConfigurationsManager.setServerConfig(JsonHandler.toParamConfigurations(result));
                        projectLoad();
                        break;
                    case REST_PDFREPORTREQUEST_KEY:
                        if (ProjectHandler.isLastReport(project, result)) {
                            ProjectHandler.projectUpdate(project, this);
                        }
                        break;
                    case REST_PROJECTSAVE_KEY:
                        startProjectEdit();
                        break;
                    case REST_FIRSTCONNECTIONTEST_KEY:
                        if (result.toLowerCase().equals("success")) {
                            identify();
                        } else {
                            dataLoadError(getResources().getString(R.string.serverConnectErrorMessage), YES_NO, false, false);
                        }
                        break;
                    case REST_IDENTIFY_KEY:
                        Device device;
                        try {
                            device = JsonHandler.toDevice(result);

                            Configurations conf = ConfigurationsManager.getLocalConfig();
                            conf.setUserId(device.getId());
                            conf.setUsername(device.getName());
                            conf.setRole(device.getRole());
                            conf.setDeviceId(device.getDeviceid());

                            if (conf.getRole() == null || conf.getUsername() == null || !device.isEnabled()
                                    || conf.getRole().equals("") || conf.getUsername().equals("")) {
//                            requestServerAuthorization();
                                dataLoadError(getResources().getString(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")"), OK, true, true);
                            } else {
                                ConfigurationsManager.setLocalConfig(conf);
                                hasAuthorization = true;
                                layoutRestore();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            requestServerAuthorization();
                            dataLoadError(getResources().getString(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")"), OK, true, true);
                        }
                        break;
                }
            } else {
                toggleControls(false);
                projectLoad();
                dataLoadError(App.getContext().getResources().getString(R.string.drawingLockedMessage), OK, false, true);
            }
        } else {
            if (requestCode.equals(REST_FIRSTCONNECTIONTEST_KEY)) {
                dataLoadError(getResources().getString(R.string.serverConnectErrorMessage), YES_NO, false, false);
            } else if (requestCode.equals(REST_IDENTIFY_KEY)){
                requestServerAuthorization();
                dataLoadError(getResources().getString(R.string.deviceNotAuthorizedMessage).concat("\n(").concat(App.getUniqueId()).concat(")"), OK, true,true);
            } else {
                if(!hasAuthorization)
                    dataLoadError(null, OK, true, true);
                else
                    dataLoadError(null, YES_NO, false, true);
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
        Device device = new Device();
        device.setDeviceid(App.getUniqueId());
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_ASKAUTHORIZATION_KEY);
        uriBuilder.setDevice(device);
        restTemplateHelper.execute(uriBuilder);
    }

    private void firstContact(){
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
