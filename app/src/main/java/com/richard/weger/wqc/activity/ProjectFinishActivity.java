package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;
import com.richard.weger.wqc.util.AsyncFromLocalfolderToServer;
import com.richard.weger.wqc.util.LogHandler;
import com.richard.weger.wqc.util.ProjectExport;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.richard.weger.wqc.util.AppConstants.*;

public class ProjectFinishActivity extends Activity
        implements
        ProjectExport.ProjectHandlerResponse,
        AsyncFromLocalfolderToServer.AsyncFromLocalfolderToServerResponse {

    Project project = null;
    Map<String, String> mapValues = null;
    ParamConfigurations conf;
    ProgressBar progressBarOverall, progressBarOperation;
    int overallProgress = 0;
    AsyncTask asyncTask;

    @Override
    public void onBackPressed(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_finish);

        setListeners();

        mapValues = (HashMap) getIntent().getSerializableExtra(MAP_VALUES_KEY);

        init();
    }

    private void setListeners(){
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
                end();
            }
        });
    }

    private void init(){
        progressBarOverall = findViewById(R.id.progressBarOverall);
        progressBarOverall.setIndeterminate(false);
        progressBarOverall.setMax(100);

        progressBarOperation = findViewById(R.id.progressBarOperation);
        progressBarOperation .setIndeterminate(false);
        progressBarOperation .setMax(100);

        progressUpdate(false, overallProgress, 0);
    }

    private void end(){
        if(asyncTask != null && (asyncTask.getStatus().equals(AsyncTask.Status.RUNNING)
        || asyncTask.getStatus().equals(AsyncTask.Status.PENDING))){
            asyncTask.cancel(true);
        }
        finish();
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

        overallProgress = 0;
        progressUpdate(true, overallProgress, 0);

        b = getIntent().getExtras();
        project = (Project) b.get(PROJECT_KEY);
        report = project.getDrawingRefs().get(0).getReports().get(CONTROL_CARD_REPORT_ID);
//        report.setClient(editClient.getText().toString());
//        report.setResponsible(editResponsible.getText().toString());
//        report.setComments(editComments.getText().toString());
        report.setDate(Calendar.getInstance().getTime());
        asyncTask = (new ProjectExport(this)).execute();
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
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.show();
    }

    private void progressUpdate(boolean status, int overallProgress, int operationProgress){
        progressBarOverall.setProgress(overallProgress, true);
        progressBarOperation.setProgress(operationProgress, true);
        if(status) {
            progressBarOverall.setVisibility(View.VISIBLE);
            progressBarOperation.setVisibility(View.VISIBLE);
            (findViewById(R.id.btnReportSubmit)).setEnabled(false);
            (findViewById(R.id.editClient)).setEnabled(false);
            (findViewById(R.id.editReportComments)).setEnabled(false);
            (findViewById(R.id.editResponsible)).setEnabled(false);
            (findViewById(R.id.tvOperationProgress)).setVisibility(View.VISIBLE);
            (findViewById(R.id.tvOverallProgress)).setVisibility(View.VISIBLE);
        }
        else{
            progressBarOverall.setVisibility(View.GONE);
            progressBarOperation.setVisibility(View.GONE);
            (findViewById(R.id.btnReportSubmit)).setEnabled(true);
            (findViewById(R.id.editClient)).setEnabled(true);
            (findViewById(R.id.editReportComments)).setEnabled(true);
            (findViewById(R.id.editResponsible)).setEnabled(true);
            (findViewById(R.id.tvOperationProgress)).setVisibility(View.INVISIBLE);
            (findViewById(R.id.tvOverallProgress)).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void ProjectHandlerCallback(String inputPath) {
        String serverPath = "smb://"
                .concat(conf.getServerPath()
                .concat(conf.getRootPath()
                .concat(mapValues.get(COMMON_PATH_KEY)
                .concat("QualityControl/"))));
        LogHandler.writeData("Starting project's upload routine", getExternalFilesDir(null));
//        serverPath = serverPath.concat(StringHandler.getProjectName(project));
        overallProgress = 50;
        progressUpdate(true, overallProgress, 0);
        //asyncTask = (new AsyncFromLocalfileToServer(this, conf)).execute(serverPath, serverPath, inputPath.concat(".zip"));
        asyncTask = (new AsyncFromLocalfolderToServer(this, conf)).execute(serverPath, inputPath);
    }

    @Override
    public void ProjectHandlerProgressUpdate(int currentProgress) {
        progressUpdate(true, overallProgress, currentProgress);
    }

    @Override
    public void AsyncFromLocalfolderToServerProgressUpdate(int currentProgress) {
        progressUpdate(true, overallProgress, currentProgress);
    }

    @Override
    public void AsyncFromLocalfolderToServerCallback(boolean bResult, String serverPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectFinishActivity.this);
        overallProgress = 100;
        progressUpdate(true, overallProgress, 0);
        if(bResult){
            builder.setMessage(R.string.successfulServerUploadMessage);
            builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        } else {
            builder.setTitle(R.string.unknownErrorMessage);
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
        progressUpdate(false, overallProgress, 0);
    }
}
