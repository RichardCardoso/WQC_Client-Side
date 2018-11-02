package com.richard.weger.wqc.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.ReportAdapter;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.firebird.FirebirdMessagingService;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.ConfigurationsManager;

import static com.richard.weger.wqc.helper.LogHelper.*;

import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.ReportHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.util.ProjectExport;

import java.util.Locale;
import java.util.Map;

import static com.richard.weger.wqc.constants.AppConstants.*;

public class ProjectEditActivity extends ListActivity implements ReportAdapter.ChangeListener, RestTemplateHelper.RestHelperResponse, FirebirdMessagingService.FirebaseListener {

    ParamConfigurations conf;
    Project project;
    Locale locale ;
    ReportAdapter reportAdapter;
    Intent intent = null;

    Runnable runnable;
    Handler handler = new Handler();
    boolean paused = false;

    private void setRunnable(){
        final int interval = 1000;
        runnable = new Runnable(){
            public void run(){
                if(!checkInternetConnection()){
                    setWaitingLayout();
                    paused = true;
                } else {
                    inflateActivityLayout();
                    if(paused){
                        projectLoad();
                        paused = false;
                    }
                }
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);
    }

    private void setWaitingLayout(){
        setContentView(R.layout.activity_wait);
        (findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
        ((findViewById(R.id.btnExit))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                onDestroy();
            }
        });
    }

    private boolean checkInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onBackPressed(){}

    @Override
    public void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume(){
        super.onResume();
        writeData("Activity resumed. Starting project load routine");
        projectLoad();
        FirebirdMessagingService.delegate = this;
        setRunnable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateActivityLayout();
        writeData("Getting project data from previous activity intent");
        Intent intent = getIntent();
        project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        writeData("Starting routine to link the project's references");
        ProjectHelper.linkReferences(project);
        writeData("Starting init routine from project edit screen");
        init();
    }

    private void inflateActivityLayout(){
        writeData("Started layout inflate activity");
        setContentView(R.layout.activity_project_edit);
        setListeners();
        if(project != null){
            setFields();
        }
        locale = getResources().getConfiguration().locale;
    }

    private void init(){
        writeData("Started init routine from project edit screen");
        setFields();
        setConf();
        setAdapter();
        writeData("Finished init routine from project edit screen");
    }

    private void toggleControls(boolean bResume){
        writeData("Started toggle controls routine");
        getListView().setClickable(bResume);
        reportAdapter.setEnabled(bResume);
        reportAdapter.notifyDataSetChanged();
        if(!bResume)
            findViewById(R.id.btnProjectFinish).setEnabled(bResume);
        else
            findViewById(R.id.btnProjectFinish).setEnabled(ProjectHelper.isEverythingFinished(project));
        if(bResume){
            findViewById(R.id.progressBarProjectMain).setVisibility(View.INVISIBLE);
        }
        else{
            findViewById(R.id.progressBarProjectMain).setVisibility(View.VISIBLE);
        }
        writeData("Finished toggle controls routine");
    }

    private void setConf(){
        writeData("Started saved configs load");
        conf = ConfigurationsManager.getServerConfig();
        writeData("Finished saved configs load");
    }

    private void setAdapter(){
        writeData("Started adapters set routine");
        reportAdapter = new ReportAdapter(this, project.getDrawingRefs().get(0).getReports());
        setListAdapter(reportAdapter);
        reportAdapter.setChangeListener(this);
        writeData("Finished adapters set routine");
    }

    private void setListeners(){
        writeData("Started listeners set routine");
        Button button;

        button = findViewById(R.id.btnProjectFinish);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(ProjectEditActivity.this,R.string.notImplementedMessage, Toast.LENGTH_LONG).show();

                if(ProjectHelper.isEverythingFinished(project)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProjectEditActivity.this);
                    builder.setTitle(R.string.projectFinishButton);
                    builder.setMessage(R.string.projectFinishConfirmation);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            writeData("Starting project finish activity");
                            Intent intent = new Intent(ProjectEditActivity.this, ProjectFinishActivity.class);
                            intent.putExtra(PROJECT_KEY, project);
                            startActivityForResult(intent, PROJECT_FINISH_SCREEN_ID);
                        }
                    });
                    builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProjectEditActivity.this);
                    builder.setTitle(R.string.unableToProcced);
                    builder.setMessage(R.string.unfinishedReportsMessage);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
            }
        });

        button = findViewById(R.id.btnExit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProjectEditActivity.this);
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
        writeData("Finished listeners set routine");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        writeData("Started activity result handle routine");
        Bundle b;
        if(data != null) {
            b = data.getExtras();
        }
        if(resultCode == RESULT_CANCELED){
            writeData("Close app request received from the user");
            switch(requestCode){
                case CHECK_REPORT_EDIT_SCREEN_KEY:
                    writeData("The sender was the check report edit screen");
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
            }
        } else {
            writeData("User finished edition of the check report");
            writeData("Started get updated project from server routine");
            toggleControls(false);
            projectLoad();
        }
        reportAdapter.notifyDataSetChanged();
        writeData("Finished activity result handle routine");
    }

    private void setFields(){
        writeData("Started trial to set the Project main activity's fields values.");
        String projectNumber = project.getReference();
        String drawingNumber = String.valueOf(project.getDrawingRefs().get(0).getNumber());
        String partNumber = String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber());

        project.setReference(projectNumber);
        ((TextView)findViewById(R.id.tvProjectInfo)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.projectNumberPrefix), projectNumber));

        project.getDrawingRefs().get(0).setNumber(Integer.valueOf(drawingNumber));
        ((TextView)findViewById(R.id.tvReportType)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.drawingNumberPrefix), drawingNumber));

        project.getDrawingRefs().get(0).getParts().get(0).setNumber(Integer.valueOf(partNumber));
        ((TextView)findViewById(R.id.tvPartNumber)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.partNumberPrefix), partNumber));

        writeData("Successful finished trial to set the Project main activity's fields values.");
    }

     @Override
    public void reportListClick(Report report, int position) {
         writeData("Started report list click routine");
        Class targetActivityClass;
        toggleControls(false);
        targetActivityClass = (new ReportHelper()).getTargetActivityClass(report);
         writeData("Starting report edit screen");
        intent = new Intent(ProjectEditActivity.this, targetActivityClass);
        intent.putExtra(REPORT_KEY, report);
        intent.putExtra(PROJECT_KEY, project);
        intent.putExtra(REPORT_ID_KEY, position);
        projectLoad();
    }

    private void projectLoad(){
        writeData("Started project download from server routine");
        toggleControls(false);
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_QRPROJECTLOAD_KEY);
        uriBuilder.getParameters().add(StringHelper.getQrText(project));
        restTemplateHelper.execute(uriBuilder);
        writeData("Finished project download from server routine");
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        writeData("Started server response handling routine");
        if(result != null) {
            if (requestCode.equals(REST_QRPROJECTLOAD_KEY)) {
                writeData("The request was a qr project load one");
                if (!result.equals("")) {
                    writeData("Started trial to parse the result to a project entity");
                    project = ProjectHelper.fromJson(result);
                    ProjectHelper.linkReferences(project);
                    if (project == null) {
                        writeData("Error while trying to parse result to project");
                        dataLoadError();
                    } else {
                        writeData("Parse successful");
                        init();
                        reportAdapter.notifyDataSetChanged();
                        toggleControls(true);
                        setFields();
                        if(intent != null){
                            writeData("Starting check report edit screen");
                            startActivityForResult(intent, CHECK_REPORT_EDIT_SCREEN_KEY);
                            intent = null;
                        }
                    }
                }
            }
        }
    }

    private void dataLoadError(){
        writeData("Started error message show routine");
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getResources().getString(R.string.unknownErrorMessage)
                .concat(". ")
                .concat(getResources().getString(R.string.tryAgainMessage));
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.setNegativeButton(R.string.noTag, null);
        builder.show();
        writeData("Finished error message show routine");
    }

    @Override
    public void messageReceived(Map<String, String> data) {
        String qrCode = data.get("qrCode");
        if(StringHelper.getQrText(project).equals(qrCode)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    projectLoad();
                }
            });
        }
    }
}
