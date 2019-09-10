package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.MessageboxHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.service.ReportRequestParametersResolver;

import java.util.ArrayList;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.PARAMCONFIG_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_REPORTUPLOAD_KEY;

public class ProjectInfoActivity extends Activity implements EntityRestTemplateHelper.RestTemplateResponse {

    Project project = null;
    ParamConfigurations conf = null;
    List<EntityRestTemplateHelper> queue = new ArrayList<>();

    @Override
    public void onBackPressed(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);

        findViewById(R.id.pbProjectFinish).setVisibility(View.INVISIBLE);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            project = (Project) b.get(PROJECT_KEY);
            conf = (ParamConfigurations) b.get(PARAMCONFIG_KEY);
            ProjectHelper.linkReferences(project);
        } else {
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_INTENT_DATA_RETRIEVAL_EXCEPTION, getResources().getString(R.string.unknownErrorMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
            ErrorResponseHandler.handle(err, this, () -> close(true));
        }

        setListeners();
        setFields();
    }

    private void setFields(){
        EditText editText;
        for(Report r : project.getDrawingRefs().get(0).getReports()){
            if(r instanceof ItemReport){
                editText = findViewById(R.id.editClient);

                if(((ItemReport) r).getClient() != null)
                    editText.setText(((ItemReport) r).getClient());
                editText = findViewById(R.id.editReportComments);

                if(((ItemReport) r).getComments() != null)
                    editText.setText(((ItemReport) r).getComments());

            }
            editText = findViewById(R.id.editResponsible);
            editText.setText(DeviceHelper.getCurrentDevice().getName());

        }
    }

    private void setListeners(){
        Button btn = findViewById(R.id.btnProjectSave);
        btn.setOnClickListener(view -> reportSubmit());
        btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(view -> close(false));
    }

    private void close(boolean error){
        if(error){
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    private void reportSubmit() {
        EditText editClient, editComments;

        toggleControls(false);
        editClient = findViewById(R.id.editClient);
        editComments = findViewById(R.id.editReportComments);
        if (editClient.getText().toString().equals("")) {
            String message = getResources().getString(R.string.emptyFieldsError);
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_EMPTY_FIELDS_WARNING, message, ErrorResult.ErrorLevel.WARNING, getClass());
            ErrorResponseHandler.handle(err, this, () -> toggleControls(true));
            return;
        }

        for (Report r : project.getDrawingRefs().get(0).getReports()) {
            if (r instanceof ItemReport) {
                ItemReport report = (ItemReport) r;
                report.setClient(editClient.getText().toString());
                report.setComments(editComments.getText().toString());

                ReportRequestParametersResolver resolver = new ReportRequestParametersResolver(REST_REPORTUPLOAD_KEY, conf, false);
                queue.add(resolver.postEntity(report, this));
            }
        }

    }

    @Override
    public void toggleControls(boolean bResume){
        (findViewById(R.id.btnCancel)).setEnabled(bResume);
        (findViewById(R.id.btnProjectSave)).setEnabled(bResume);
        (findViewById(R.id.editClient)).setEnabled(bResume);
        (findViewById(R.id.editReportComments)).setEnabled(bResume);
        if(bResume){
            (findViewById(R.id.pbProjectFinish)).setVisibility(View.INVISIBLE);
        } else {
            (findViewById(R.id.pbProjectFinish)).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFatalError() {

    }

    private void completion(){
        if(queue != null && (queue.stream().filter(r -> r.getStatus() == AsyncTask.Status.FINISHED || r.isCancelled() || r.getStatus() == AsyncTask.Status.PENDING).count() - 1) <= 0) {
            String message = getResources().getString(R.string.changesSavedMessage);
            MessageboxHelper.showMessage(this,
                    message,
                    getResources().getString(R.string.okTag),
                    () -> close(false)
            );
        }
    }

    @Override
    public  void RestTemplateCallback(AbstractResult result) {
        if (result instanceof SuccessResult && result.getRequestCode().equals(REST_REPORTUPLOAD_KEY)) {
            completion();
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, () -> close(false));
        }
    }

}
