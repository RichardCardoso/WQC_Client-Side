package com.richard.weger.wqc.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.ReportAdapter;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.firebird.FirebaseHelper;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.ReportHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.LoggerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.PARAMCONFIG_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_LIST_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_FINISH_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_ID_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_CONFIGLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.WELCOME_ACTIVITY_SCREEN_KEY;

public class ProjectEditActivity extends ListActivity
        implements ReportAdapter.ChangeListener,
        RestTemplateHelper.RestResponseHandler,
                    FirebaseHelper.FirebaseListener {

    ParamConfigurations conf;
    Project project;
    Locale locale ;
    ReportAdapter reportAdapter;
    List<Report> reports = new ArrayList<>();

    Logger logger;

    @Override
    public void onBackPressed(){}


    @Override
    public void onResume(){
        super.onResume();
        logger = LoggerManager.getLogger(getClass());
        logger.info("Activity resumed. Starting project load routine");
        loadServerConfig();
    }

    private void loadServerConfig(){
        ConfigurationsManager.loadServerConfig(this);
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
        inflateActivityLayout();
        toggleControls(false);
        setFields();
        setAdapter();
        toggleControls(true);
    }

    @Override
    public void toggleControls(boolean bResume){
        logger.info("Started toggle controls routine");
        if(getListView() != null) {
            getListView().setClickable(bResume);
        }
        if(reportAdapter != null) {
            reportAdapter.setEnabled(bResume);
            reportAdapter.notifyDataSetChanged();
        }
        if(findViewById(R.id.btnProjectInfo) != null) {
            findViewById(R.id.btnProjectInfo).setEnabled(bResume);
        }

        if (!bResume && findViewById(R.id.progressBarProjectMain) != null) {
            findViewById(R.id.progressBarProjectMain).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.progressBarProjectMain).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onFatalError() {

    }

    private void setAdapter(){
        logger.info("Started adapters set routine");
        reportAdapter = new ReportAdapter(this, reports);
        setListAdapter(reportAdapter);
        reportAdapter.setChangeListener(this);
    }

    private void setListeners(){
        logger.info("Started listeners set routine");
        ImageButton button;

        button = findViewById(R.id.btnCustomPictures);
        button.setOnClickListener(v -> {
            logger.info("Started general pictures list activity");
            toggleControls(false);
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
        button.setOnClickListener(v -> AlertHelper.showMessage(this,
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
                () -> AlertHelper.showMessage(this,
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

    private void startReportEdit(Long id) {
        logger.info("Starting report edit activity");
        Report report = project.getDrawingRefs().get(0).getReports().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if(report != null) {
            toggleControls(false);
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

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        logger.info("Started server response handling routine");
        if (result instanceof SuccessResult) {
            if (result.getRequestCode().equals(REST_QRPROJECTLOAD_KEY)) {
                logger.info("Got project from received request");
                project = ResultService.getSingleResult(result, Project.class);
                ProjectHelper.linkReferences(project);
                reports.clear();
                reports.addAll(project.getDrawingRefs().get(0).getReports());
                init();
//                reportAdapter.notifyDataSetChanged();
            } else if (result.getRequestCode().equals(REST_CONFIGLOAD_KEY)){
                logger.info("Got ParamConfigurations from received request");
                ParamConfigurations c = ResultService.getSingleResult(result, ParamConfigurations.class);
                ConfigurationsManager.setServerConfig(c);
                conf = c;
                FirebaseHelper.firebaseConfig(this);
            }
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, null);
        }
    }

    @Override
    public void messageReceived(String qrCode, Long id) {
        runOnUiThread(() -> ProjectHelper.projectLoad(this, false));
    }

    @Override
    public void onConnectionSuccess() {
        ProjectHelper.projectLoad(this);
    }

}
