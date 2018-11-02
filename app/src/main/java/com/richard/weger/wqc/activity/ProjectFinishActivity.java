package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.ReportHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.DeviceManager;
import com.richard.weger.wqc.util.ProjectExport;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;

import static com.richard.weger.wqc.constants.AppConstants.*;
import static com.richard.weger.wqc.helper.LogHelper.writeData;

public class ProjectFinishActivity extends Activity implements RestTemplateHelper.RestHelperResponse {

    Project project = null;
    List<RestTemplateHelper> requests = new ArrayList<>();

    @Override
    public void onBackPressed(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_finish);

        findViewById(R.id.pbProjectFinish).setVisibility(View.INVISIBLE);

        Bundle b = getIntent().getExtras();
        project = (Project) b.get(PROJECT_KEY);
        ProjectHelper.linkReferences(project);

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
            editText.setText(DeviceManager.getCurrentDevice().getName());
        }
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
                close(false);
            }
        });
    }

    private void close(boolean error){
        if(error){
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    private void confirmUpload(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectFinishActivity.this);
        builder.setTitle(R.string.confirmationNeeded);
        builder.setMessage(R.string.projectUploadMessage);
        builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                projectSubmit();
            }
        });
        builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.show();
    }

    private void projectSubmit() {
        EditText editClient, editComments;

        toggleControls(false);
        editClient = findViewById(R.id.editClient);
        editComments = findViewById(R.id.editReportComments);
        if (editClient.getText().toString().equals("")) {
            Toast.makeText(this, R.string.emptyFieldsError, Toast.LENGTH_LONG).show();
            return;
        }

        for (Report r : project.getDrawingRefs().get(0).getReports()) {
            if (r instanceof ItemReport) {
                ((ItemReport) r).setClient(editClient.getText().toString());
                ((ItemReport) r).setComments(editComments.getText().toString());
            }
        }
        projectSave();
    }

    private void projectSave(){
        ProjectHelper.projectUpdate(project, this);
    }

    private void projectUpload(){
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PROJECTUPLOAD_KEY);
        uriBuilder.setProject(project);
        restTemplateHelper.execute(uriBuilder);
    }

    private void toggleControls(boolean bResume){
        (findViewById(R.id.btnReportSubmit)).setEnabled(bResume);
        (findViewById(R.id.editClient)).setEnabled(bResume);
        (findViewById(R.id.editReportComments)).setEnabled(bResume);
        if(bResume){
            (findViewById(R.id.pbProjectFinish)).setVisibility(View.INVISIBLE);
        } else {
            (findViewById(R.id.pbProjectFinish)).setVisibility(View.VISIBLE);
        }
    }

    private void dataLoadError(String message){
        writeData("Showing data load error default message");
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(message == null) {
            message = getResources().getString(R.string.dataRecoverError);
        }
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                close(true);
            }
        });
        builder.show();
    }

    private void pictureUpload(Report report, Item item, RestTemplateHelper restTemplateHelper){
        writeData("Started picture upload request");
        toggleControls(false);

        String picName = item.getPicture().getFileName();
        picName = picName.substring(picName.lastIndexOf("/") + 1);

        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PICTUREUPLOAD_KEY);
        uriBuilder.setReport(report);
        uriBuilder.setItem(item);
        uriBuilder.setProject(report.getDrawingref().getProject());
        uriBuilder.getParameters().add(picName);
        restTemplateHelper.execute(uriBuilder);
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        if(result != null) {
            if (!result.equals(App.getContext().getResources().getString(R.string.drawingLockedMessage))) {
                if(requestCode.equals(REST_PROJECTSAVE_KEY)){
                    projectUpload();
                } else if (requestCode.equals(REST_PROJECTUPLOAD_KEY)) {
                    if (result.equals("ok")) {
                        int cnt = 0;
                        for (Report r : project.getDrawingRefs().get(0).getReports()) {
                            if (r instanceof ItemReport) {
                                for(Item i : ((ItemReport) r).getItems()){
                                    if(FileHelper.isValidFile(StringHelper.getPictureFilePath(project, i))) {
                                        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
                                        pictureUpload(r, i, restTemplateHelper);
                                        requests.add(restTemplateHelper);
                                        cnt++;
                                    }
                                }
                            }
                        }
                        if(cnt == 0){
                            completion();
                        }
                    } else {
                        dataLoadError(getResources().getString(R.string.dataRecoverError).concat(" Message: ").concat(result));
                    }
                } else if (requestCode.equals(REST_PICTUREUPLOAD_KEY)){
                    for(int i = 0; i < requests.size(); i++){
                        RestTemplateHelper r = requests.get(i);
                        if(r.getStatus() == AsyncTask.Status.FINISHED || r.isCancelled()){
                            requests.remove(r);
                        }
                    }
                    if(requests.size() <=1){
                        completion();
                    }
                }
            } else {
                String message = "A write attempt was made but the drawing is currently locked by another user. Aborting write attempt";
                writeData(message);
                message = message.concat(".\n").concat(getResources().getString(R.string.dataRecoverError));
                toggleControls(false);
                dataLoadError(message);
            }
        } else {
            dataLoadError(null);
        }
    }

    private void completion(){
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.successfulServerUploadMessage));
        builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                close(false);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}
