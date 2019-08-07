package com.richard.weger.wqc.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.ReportAdapter;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.firebird.FirebirdMessagingService;
import com.richard.weger.wqc.helper.MessageboxHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.rest.entity.EntityRestResult;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.service.AbstractEntityRequestParametersResolver;
import com.richard.weger.wqc.service.ProjectRequestParametersResolver;
import com.richard.weger.wqc.util.ConfigurationsManager;

import static com.richard.weger.wqc.helper.LogHelper.*;

import com.richard.weger.wqc.helper.ReportHelper;
import com.richard.weger.wqc.helper.DeviceHelper;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.richard.weger.wqc.appconstants.AppConstants.*;

public class ProjectEditActivity extends ListActivity
        implements ReportAdapter.ChangeListener,
                    EntityRestTemplateHelper.EntityRestResponse,
                    FirebirdMessagingService.FirebaseListener {

    ParamConfigurations conf;
    Project project;
    Locale locale ;
    ReportAdapter reportAdapter;
    List<Report> reports = new ArrayList<>();

    Runnable runnable;
    Handler handler = new Handler();
    boolean paused = false;
    boolean resumed = true;

    private void setRunnable(){
        final int interval = 1000;
        runnable = () -> {
            if(!checkInternetConnection()){
                setWaitingLayout();
                paused = true;
            } else {
                if(paused){
                    inflateActivityLayout();
                    projectLoad();
                    paused = false;
                }
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);
    }

    private void setWaitingLayout(){
        setContentView(R.layout.activity_wait);
        (findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
        ((findViewById(R.id.btnExit))).setOnClickListener(v -> {
            finish();
            onDestroy();
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
        if (FirebirdMessagingService.delegate != this) {
            FirebirdMessagingService.delegate = this;
        }
        setRunnable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateActivityLayout();
        writeData("Getting project data from previous activity intent");
        Intent intent = getIntent();
        project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        reports = project.getDrawingRefs().get(0).getReports();
        ProjectHelper.linkReferences(project);
        init();
        resumed = false;
    }

    private void inflateActivityLayout(){
        writeData("Started layout inflate activity");
        setContentView(R.layout.activity_project_edit);

        if(DeviceHelper.isOnlyRole("TE")){
            findViewById(R.id.btnProjectInfo).setVisibility(View.INVISIBLE);
        }

        setListeners();
        if(project != null){
            setFields();
        }
        locale = getApplicationContext().getResources().getConfiguration().getLocales().get(0);
    }

    private void init(){
        writeData("Started init routine from project edit screen");
        setFields();
        setConf();
        setAdapter();
    }

    @Override
    public void toggleControls(boolean bResume){
        writeData("Started toggle controls routine");
        getListView().setClickable(bResume);
        reportAdapter.setEnabled(bResume);
        reportAdapter.notifyDataSetChanged();

        findViewById(R.id.btnProjectInfo).setEnabled(bResume);

        if(!bResume) {
            findViewById(R.id.progressBarProjectMain).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.progressBarProjectMain).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onError() {

    }

    private void setConf(){
        conf = ConfigurationsManager.getServerConfig();
    }

    private void setAdapter(){
        writeData("Started adapters set routine");
        reportAdapter = new ReportAdapter(this, reports);
        setListAdapter(reportAdapter);
        reportAdapter.setChangeListener(this);
    }

    private void setListeners(){
        writeData("Started listeners set routine");
        Button button;

        button = findViewById(R.id.btnCustomPictures);
        button.setOnClickListener(v -> {
            writeData("Started general pictures list activity");
            Intent intent = new Intent(ProjectEditActivity.this, PicturesListActivity.class);
            intent.putExtra(PROJECT_KEY, project);
            startActivityForResult(intent, PICTURE_LIST_SCREEN_ID);
        });

        button = findViewById(R.id.btnProjectInfo);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectEditActivity.this, ProjectInfoActivity.class);
            intent.putExtra(PROJECT_KEY, project);
            intent.putExtra(PARAMCONFIG_KEY, conf);
            startActivityForResult(intent, PROJECT_FINISH_SCREEN_ID);
        });

        button = findViewById(R.id.btnExit);
        button.setOnClickListener(v -> MessageboxHelper.showMessage(this,
                getResources().getString(R.string.confirmationNeeded),
                getResources().getString(R.string.qrScanQuestion),
                getResources().getString(R.string.yesTAG),
                getResources().getString(R.string.noTag),
                () -> {
                    Intent intent = new Intent(ProjectEditActivity.this, WelcomeActivity.class);
                    startActivityForResult(intent, WELCOME_ACTIVITY_SCREEN_KEY);
                    finish();
                },
                () -> MessageboxHelper.showMessage(this,
                        getResources().getString(R.string.app_name),
                        getResources().getString(R.string.closeQuestion),
                        getResources().getString(R.string.yesTAG),
                        getResources().getString(R.string.noTag),
                        this::finish,
                        null)));

    }

    private void setFields(){
        writeData("Started trial to set the Project main activity's fields values.");
        String projectNumber = project.getReference();
        String drawingNumber = String.valueOf(project.getDrawingRefs().get(0).getDnumber());
        String partNumber = String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber());

        project.setReference(projectNumber);
        ((TextView)findViewById(R.id.tvProjectInfo)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.projectNumberPrefix), projectNumber));

        project.getDrawingRefs().get(0).setDnumber(Integer.valueOf(drawingNumber));
        ((TextView)findViewById(R.id.tvReportType)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.drawingNumberPrefix), drawingNumber));

        project.getDrawingRefs().get(0).getParts().get(0).setNumber(Integer.valueOf(partNumber));
        ((TextView)findViewById(R.id.tvPartNumber)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.partNumberPrefix), partNumber));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        toggleControls(false);
    }

    private void startReportEdit(Long id) {
        writeData("Starting report edit activity");
        Report report = project.getDrawingRefs().get(0).getReports().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if(report != null) {
            ReportHelper helper = new ReportHelper();
            toggleControls(false);
            Class targetActivityClass;
            targetActivityClass = helper.getTargetActivityClass(report);
            writeData("Starting report edit screen (class: " + targetActivityClass.getSimpleName() + ")");
            Intent intent = new Intent(ProjectEditActivity.this, targetActivityClass);
            intent.putExtra(REPORT_KEY, report);
            intent.putExtra(PROJECT_KEY, project);
            intent.putExtra(REPORT_ID_KEY, report.getId());
            intent.putExtra(PARAMCONFIG_KEY, conf);
            startActivityForResult(intent, helper.getTargetActivityKey(report));
        } else {
            writeData("Report with id " + id + " not found. Edit activity will not start");
            MessageboxHelper.showMessage(this,
                    getResources().getString(R.string.unknownErrorMessage),
                    getResources().getString(R.string.okTag),
                    null);
        }
    }

     @Override
    public void reportListClick(Long id) {
        startReportEdit(id);
    }

    private void projectLoad(){
        writeData("Started project download from server routine");
        toggleControls(false);
        AbstractEntityRequestParametersResolver<Project> resolver = new ProjectRequestParametersResolver(REST_QRPROJECTLOAD_KEY, conf, true);
        resolver.getEntity(project, this);
    }

    @Override
    public void EntityRestCallback(EntityRestResult result) {
        writeData("Started server response handling routine");
        toggleControls(true);
        if (result.getRequestCode().equals(REST_QRPROJECTLOAD_KEY)) {
            writeData("The request was a qr project load one");
            if (result.getStatus() == HttpStatus.OK) {
                writeData("Started trial to parse the result to a project entity");
                project = (Project) result.getEntities().get(0);
                ProjectHelper.linkReferences(project);
                if (project == null) {
                    writeData("Error while trying to parse result to project");
                    dataLoadError();
                } else {
                    writeData("Parse successful");
                    reports.clear();
                    reports.addAll(project.getDrawingRefs().get(0).getReports());
                    reportAdapter.notifyDataSetChanged();
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
        builder.setPositiveButton(R.string.yesTAG, (dialog, which) -> {
            setResult(RESULT_OK);
            finish();
        });
        builder.setNegativeButton(R.string.noTag, null);
        builder.show();
    }

    @Override
    public void messageReceived(Map<String, String> data) {
        String qrCode = data.get("qrCode");
        if(qrCode != null) {
//            qrCode = data.replace("\\", "");
            if (ProjectHelper.getQrCode().equals(qrCode)) {
                runOnUiThread(this::projectLoad);
            }
        }
    }

}
