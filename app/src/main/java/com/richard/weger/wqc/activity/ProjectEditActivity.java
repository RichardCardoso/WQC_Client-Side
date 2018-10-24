package com.richard.weger.wqc.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.ReportAdapter;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.ConfigurationsManager;

import static com.richard.weger.wqc.util.LogHandler.*;

import com.richard.weger.wqc.util.FileHandler;
import com.richard.weger.wqc.util.ProjectHandler;
import com.richard.weger.wqc.util.ReportHelper;
import com.richard.weger.wqc.util.StringHandler;
import com.richard.weger.wqc.util.JsonHandler;

import java.util.Locale;

import static com.richard.weger.wqc.util.AppConstants.*;

public class ProjectEditActivity extends ListActivity implements ReportAdapter.ChangeListener, RestTemplateHelper.HttpHelperResponse {

    ParamConfigurations conf;
    Project project;
    Locale locale ;
    ReportAdapter reportAdapter;
    Intent intent = null;

    @Override
    public void onBackPressed(){}

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(project != null && !project.getReference().equals("")) {
//            updatePendingItemsInfo();
            save();
        }
    }

    private void save(){
        if(project != null && !project.getReference().equals("")) {
            if (!project.getReference().equals(""))
                JsonHandler.toJson(project);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateActivityLayout();
        Intent intent = getIntent();
        project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        ProjectHandler.linkReferences(project);
        init();
    }

    private void inflateActivityLayout(){
        setContentView(R.layout.activity_project_edit);
        setListeners();
        locale = getResources().getConfiguration().locale;
    }

    private void init(){
        setFields();
        setConf();
        setAdapter();
        toggleProjectFinishButtonState();
        //toggleControls(ProjectHandler.isEverythingFinished(project));
    }

    private void toggleProjectFinishButtonState(){
        (findViewById(R.id.btnProjectFinish)).setEnabled(
                ProjectHandler.isEverythingFinished(project)
        );
    }

    private void toggleControls(boolean bResume){
        getListView().setClickable(bResume);
        findViewById(R.id.btnProjectFinish).setEnabled(ProjectHandler.isEverythingFinished(project));
        if(bResume){
            findViewById(R.id.progressBarProjectMain).setVisibility(View.INVISIBLE);
        }
        else{
            findViewById(R.id.progressBarProjectMain).setVisibility(View.VISIBLE);
        }
    }

    private void setConf(){
        writeData("Started saved configs load", getExternalFilesDir(null));
        conf = ConfigurationsManager.getServerConfig();
        writeData("Finished saved configs load", getExternalFilesDir(null));
    }

    private void setAdapter(){
        reportAdapter = new ReportAdapter(this, project.getDrawingRefs().get(0).getReports());
        setListAdapter(reportAdapter);
        reportAdapter.setChangeListener(this);
    }

    private void setListeners(){
        Button button;

        button = findViewById(R.id.btnProjectFinish);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProjectEditActivity.this,R.string.notImplementedMessage, Toast.LENGTH_LONG).show();

//                if(ProjectHandler.isEverythingFinished(project)) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ProjectEditActivity.this);
//                    builder.setTitle(R.string.projectFinishButton);
//                    builder.setMessage(R.string.projectFinishConfirmation);
//                    builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            /*
//                            writeData("Starting project finish activity", getExternalFilesDir(null));
//                            Intent intent = new Intent(ProjectEditActivity.this, ProjectFinishActivity.class);
//                            intent.putExtra(CONTROL_CARD_REPORT_FILE_KEY,
//                                    StringHandler.generateFileName(project, "xls"));
//                            intent.putExtra(MAP_VALUES_KEY, (HashMap) mapValues);
//                            intent.putExtra(PROJECT_KEY, project);
//                            startActivityForResult(intent, PROJECT_FINISH_SCREEN_ID);
//                            */
//                        }
//                    });
//                    builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    builder.show();
//                }
//                else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ProjectEditActivity.this);
//                    builder.setTitle(R.string.unableToProcced);
//                    builder.setMessage(R.string.unfinishedReportsMessage);
//                    builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    builder.show();
//                }
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Bundle b;
        if(data != null) {
            b = data.getExtras();
        }
        if(resultCode == RESULT_CANCELED){
            switch(requestCode){
                case CHECK_REPORT_EDIT_SCREEN_KEY:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
            }
        } else {
            toggleControls(false);
            projectLoad();
        }
        toggleProjectFinishButtonState();
    }

    private void setFields(){
        writeData("Started trial to set the Project main activity's fields values.", getExternalFilesDir(null));
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

        for(Report rep :project.getDrawingRefs().get(0).getReports()){
            if(rep instanceof ItemReport) {
                ItemReport r = (ItemReport) rep;
                project.getDrawingRefs().get(0).setNumber(Integer.valueOf(drawingNumber));
                project.getDrawingRefs().get(0).getParts().get(0).setNumber(Integer.valueOf(partNumber));
                for (Item i : r.getItems()) {
                    if (i.getPicture().getFilePath() == null || !FileHandler.isValidFile(i.getPicture().getFilePath())) {
                        i.getPicture().setFilePath("");
                    }
                    if (i.getPicture().getFilePath().equals("")) {
                        i.getPicture().setFilePath(StringHandler.generatePictureName(project, i, r));
                    }
                }
            }
        }
        writeData("Finished trial to set the Project main activity's fields values.", getExternalFilesDir(null));
    }

     @Override
    public void reportListClick(Report report, int position) {
        Class targetActivityClass;
        toggleControls(false);
        targetActivityClass = (new ReportHelper()).getTargetActivityClass(report);
        intent = new Intent(ProjectEditActivity.this, targetActivityClass);
        intent.putExtra(REPORT_KEY, report);
        intent.putExtra(PROJECT_KEY, project);
        intent.putExtra(REPORT_ID_KEY, position);
        projectLoad();
    }

    private void projectLoad(){
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_QRPROJECTLOAD_KEY);
        uriBuilder.getParameters().add(StringHandler.createQrText(project));
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        if(result != null) {
            if (requestCode.equals(REST_QRPROJECTLOAD_KEY)) {
                if (!result.equals("")) {
                    project = JsonHandler.toProject(result);
                    if (project == null) {
                        dataLoadError();
                    } else {
                        init();
//                        reportAdapter.notifyDataSetChanged();
                        toggleControls(true);
                        if(intent != null){
                            startActivityForResult(intent, CHECK_REPORT_EDIT_SCREEN_KEY);
                            intent = null;
                        }
                    }
                }
            }
        }
    }

    private void dataLoadError(){
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
    }
}
