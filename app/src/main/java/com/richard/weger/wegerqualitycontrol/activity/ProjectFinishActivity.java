package com.richard.weger.wegerqualitycontrol.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Configurations;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;
import com.richard.weger.wegerqualitycontrol.util.AsyncFromLocalfileToServer;
import com.richard.weger.wegerqualitycontrol.util.ConfigurationsManager;
import com.richard.weger.wegerqualitycontrol.util.ProjectHandler;
import com.richard.weger.wegerqualitycontrol.util.StringHandler;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public class ProjectFinishActivity extends Activity implements AsyncFromLocalfileToServer.AsyncFromLocalfileToServerResponse, ProjectHandler.ProjectHandlerResponse{

    Project project = null;
    Map<String, String> mapValues = null;
    Configurations conf;
    ProgressBar progressBar;

    @Override
    public void onBackPressed(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_finish);

        Button btn = findViewById(R.id.btnReportSubmit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmUpload();
            }
        });
        btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mapValues = (HashMap) getIntent().getSerializableExtra(MAP_VALUES_KEY);

        conf = ConfigurationsManager.loadConfig(this);
        progressBar = findViewById(R.id.progressBarProjectFinish);
        progressUpdate(false);
    }

    private void reportSubmit(){
        Report report;
        Bundle b;
        EditText editClient, editResponsible, editComments;

        editClient = findViewById(R.id.editClient);
        editResponsible = findViewById(R.id.editResponsible);
        editComments = findViewById(R.id.editReportComments);
        if(editClient.getText().toString().equals("") || editResponsible.getText().toString().equals("")){
            Toast.makeText(this, R.string.emptyFieldsError, Toast.LENGTH_LONG).show();
            return;
        }

        progressUpdate(true);

        b = getIntent().getExtras();
        project = (Project) b.get(PROJECT_KEY);
        report = project.getReportList().get(CONTROL_CARD_REPORT_ID);
        report.setClient(editClient.getText().toString());
        report.setResponsible(editResponsible.getText().toString());
        report.setComments(editComments.getText().toString());
        report.setDate(Calendar.getInstance().getTime());

        ProjectHandler projectHandlerResponse = new ProjectHandler(getResources(), b,getExternalFilesDir(null), conf,project,this);
        projectHandlerResponse.run();
    }

    private void confirmUpload(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectFinishActivity.this);
        builder.setTitle(R.string.confirmationNeeded);
        builder.setMessage(R.string.projectUploadMessage);
        builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reportSubmit();
            }
        });
        builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void progressUpdate(boolean status){
        if(status) {
            progressBar.setVisibility(View.VISIBLE);
            (findViewById(R.id.btnReportSubmit)).setEnabled(false);
            (findViewById(R.id.btnCancel)).setEnabled(false);
        }
        else{
            progressBar.setVisibility(View.GONE);
            (findViewById(R.id.btnReportSubmit)).setEnabled(true);
            (findViewById(R.id.btnCancel)).setEnabled(true);
        }
    }

    @Override
    public void AsyncFromLocalfileToServerCallback(boolean bResult, String entryData, String localPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectFinishActivity.this);

        progressUpdate(false);

        builder.setTitle(R.string.confirmationNeeded);
        builder.setMessage(R.string.projectUploadMessage);
        if(bResult){
            builder.setMessage(R.string.successfulServerUploadMessage);
            builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
        else{
            builder.setMessage(getResources().getString(R.string.smbConnectError)
                    .concat(" ")
                    .concat(getResources().getString(R.string.tryAgainMessage)));
            builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reportSubmit();
                }
            });
            builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

        builder.show();
    }

    @Override
    public void ProjectHandlerCallback(String inputPath) {
        String serverPath = conf.getServerPath()
                .concat(conf.getRootPath())
                .concat(mapValues.get(COMMON_PATH_KEY).
                        concat("/QualityControl/"));
        (new AsyncFromLocalfileToServer(this, conf)).execute(serverPath.concat(StringHandler.getProjectName(project)).concat(".zip"), "", inputPath.concat(".zip"));
    }
}
