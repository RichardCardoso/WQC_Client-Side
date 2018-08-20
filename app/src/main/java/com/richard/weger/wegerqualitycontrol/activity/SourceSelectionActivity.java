package com.richard.weger.wegerqualitycontrol.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.Result;
import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Configurations;
import com.richard.weger.wegerqualitycontrol.util.AppConstants;
import com.richard.weger.wegerqualitycontrol.util.ConfigurationsManager;
import com.richard.weger.wegerqualitycontrol.util.PermissionsManager;

import java.io.File;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public class SourceSelectionActivity extends Activity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    PermissionsManager permissionsManager = new PermissionsManager();
    File rootPath;

    @Override
    public void onPause(){
        super.onPause();
        if(mScannerView != null)
            mScannerView.stopCamera();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmationNeeded);
        builder.setMessage(R.string.closeMessage);
        builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.putExtra(CLOSE_REASON, CLOSE_REASON_USER_FINISH);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_selection);

        if(!permissionsManager.checkPermission(CAMERA_PERMISSION, this, false)){
            permissionsManager.askPermission(CAMERA_PERMISSION, this);
        }
        rootPath  = this.getFilesDir();
        setListeners();
    }


    public void QrScan(){
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    private void autenticate(){
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
                config = ConfigurationsManager.loadConfig(SourceSelectionActivity.this);
                String password;
                password = input.getText().toString();
                if(config.getAppPassword().equals(password)){
                    Intent intent = new Intent(SourceSelectionActivity.this,
                            ConfigurationsActivity.class);
                    startActivityForResult(intent, CONFIG_SCREEN_KEY);
                }
                else{
                    Toast.makeText(SourceSelectionActivity.this,
                            R.string.invalidPasswordMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();
    }

    private void setListeners(){

        Button button = findViewById(R.id.btnConfig);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autenticate();
            }
        });

        findViewById(R.id.buttonQrScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QrScan();
            }
        });
        findViewById(R.id.btnContinueProject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SourceSelectionActivity.this,
                        FileSelectActivity.class);
                startActivityForResult(intent, CONTINUE_PROJECT_SCREEN_KEY);
            }
        });
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent intent = new Intent();
        intent.putExtra(SOURCE_CODE_KEY, SOURCE_CODE_QR);
        intent.putExtra(QR_CODE_KEY, rawResult.getText());
        mScannerView.stopCamera();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!permissionsManager.checkPermission(CAMERA_PERMISSION, this, false)){
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == CONTINUE_PROJECT_SCREEN_KEY){
                String fileName;
                if(data != null){
                    fileName = data.getStringExtra(CONTINUE_CODE_KEY);
                    Intent intent = new Intent();
                    intent.putExtra(SOURCE_CODE_KEY, SOURCE_CODE_CONTINUE);
                    intent.putExtra(CONTINUE_CODE_KEY, fileName);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }

    }
}
