package com.richard.weger.wqc.activity;

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
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.AbstractEntityRequestParametersResolver;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.service.ProjectRequestParametersResolver;
import com.richard.weger.wqc.util.ConfigurationsManager;

import com.richard.weger.wqc.helper.ReportHelper;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.util.LoggerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.*;

public class ProjectEditActivity extends ListActivity
        implements ReportAdapter.ChangeListener,
        EntityRestTemplateHelper.RestTemplateResponse,
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

    Logger logger;

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
        logger.info("Activity resumed. Starting project load routine");
        projectLoad();
        if (FirebirdMessagingService.delegate != this) {
            FirebirdMessagingService.delegate = this;
        }
        setRunnable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = LoggerManager.getLogger(getClass());

        inflateActivityLayout();
        logger.info("Getting project data from previous activity intent");
        Intent intent = getIntent();
        project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        reports = project.getDrawingRefs().get(0).getReports();
        ProjectHelper.linkReferences(project);
        init();
        resumed = false;
    }

    private void inflateActivityLayout(){
        logger.info("Started layout inflate activity");
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
        logger.info("Started init routine from project edit screen");
        setFields();
        setConf();
        setAdapter();
    }

    @Override
    public void toggleControls(boolean bResume){
        logger.info("Started toggle controls routine");
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
    public void onFatalError() {

    }

    private void setConf(){
        conf = ConfigurationsManager.getServerConfig();
    }

    private void setAdapter(){
        logger.info("Started adapters set routine");
        reportAdapter = new ReportAdapter(this, reports);
        setListAdapter(reportAdapter);
        reportAdapter.setChangeListener(this);
    }

    private void setListeners(){
        logger.info("Started listeners set routine");
        Button button;

        button = findViewById(R.id.btnCustomPictures);
        button.setOnClickListener(v -> {
            logger.info("Started general pictures list activity");
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
                    ProjectHelper.setQrCode("");
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
        logger.info("Started trial to set the Project main activity's fields values.");
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
        logger.info("Starting report edit activity");
        Report report = project.getDrawingRefs().get(0).getReports().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if(report != null) {
            ReportHelper helper = new ReportHelper();
            toggleControls(false);
            Class targetActivityClass;
            targetActivityClass = helper.getTargetActivityClass(report);
            logger.info("Starting report edit screen (class: " + targetActivityClass.getSimpleName() + ")");
            Intent intent = new Intent(ProjectEditActivity.this, targetActivityClass);
            intent.putExtra(REPORT_KEY, report);
            intent.putExtra(PROJECT_KEY, project);
            intent.putExtra(REPORT_ID_KEY, report.getId());
            intent.putExtra(PARAMCONFIG_KEY, conf);
            startActivityForResult(intent, helper.getTargetActivityKey(report));
        } else {
            String message = "Report with id " + id + " not found. Edit activity will not start";
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.ENTITY_NOT_FOUND, message, ErrorResult.ErrorLevel.SEVERE, getClass());
            ErrorResponseHandler.handle(err, this, null);
        }
    }

     @Override
    public void reportListClick(Long id) {
        startReportEdit(id);
    }

    private void projectLoad(){
        logger.info("Started project download from server routine");
        toggleControls(false);
        AbstractEntityRequestParametersResolver<Project> resolver = new ProjectRequestParametersResolver(REST_QRPROJECTLOAD_KEY, conf, true);
        resolver.getEntity(project, this);
    }

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        logger.info("Started server response handling routine");
        toggleControls(true);
        if (result instanceof SuccessResult) {
            logger.info("The request was a qr project load one");
            if (result.getRequestCode().equals(REST_QRPROJECTLOAD_KEY)) {
                logger.info("Started trial to parse the result to a project entity");
                project = ResultService.getSingleResult(result, Project.class);
                ProjectHelper.linkReferences(project);
                logger.info("Parse successful");
                reports.clear();
                reports.addAll(project.getDrawingRefs().get(0).getReports());
                reportAdapter.notifyDataSetChanged();
            }
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, null);
        }
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
