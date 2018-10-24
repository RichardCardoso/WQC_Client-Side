package com.richard.weger.wqc.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.DeviceManager;
import com.richard.weger.wqc.util.FileHandler;
import com.richard.weger.wqc.adapter.ItemAdapter;
import com.richard.weger.wqc.util.JsonHandler;
import com.richard.weger.wqc.util.ProjectHandler;
import com.richard.weger.wqc.util.StringHandler;

import java.util.List;

import static com.richard.weger.wqc.util.AppConstants.*;
import static com.richard.weger.wqc.util.LogHandler.writeData;

public class ItemReportEditActivity extends ListActivity implements ItemAdapter.ChangeListener, RestTemplateHelper.HttpHelperResponse {

    ItemReport report;
    Project project;
    ItemAdapter itemAdapter;
    String oldItemPicturePath;
    int lastItemId = -1;
    boolean canEdit = true;

    @Override
    public void onBackPressed() {
        /*if (!shouldAllowBack()) {
            doSomething();
        } else {
            super.onBackPressed();
        }
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_report_edit);
        Intent intent = getIntent();
        if(intent != null) {
            int id;
            id = intent.getIntExtra(REPORT_ID_KEY, -1);
            if(id < 0){
                setResult(RESULT_CANCELED);
                finish();
            }
            project = (Project) intent.getSerializableExtra(PROJECT_KEY);
            ProjectHandler.linkReferences(project);
            report = (ItemReport) project.getDrawingRefs().get(0).getReports().get(id);
        }
        if(report == null || project == null){
            Toast.makeText(this, R.string.dataRecoverError,Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        itemAdapter = new ItemAdapter(this, report.getItems());
        setListAdapter(itemAdapter);
        itemAdapter.setChangeListener(this);
        setListeners();
        setTextViews();
    }

    private void setTextViews(){
        ((TextView)findViewById(R.id.tvProjectInfo)).setText(
                String.format(getResources().getConfiguration().locale,
                        "%s - Z%d",
                        project.getReference(),
                        project.getDrawingRefs().get(0).getNumber()
//                        ,project.getDrawingRefs().getContext(0).getPart().getContext(0).getNumber()
                        )
        );
        ((TextView)findViewById(R.id.tvReportType)).setText(report.toString());


        updatePendingItemsCount();
    }

    private void setListeners(){
        Button btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String currentItemPicturePath = report.getItems().get(lastItemId).getPicture().getFilePath();
//                if(oldItemPicturePath != null
//                        &&
//                        !currentItemPicturePath.contains(oldItemPicturePath)){
//                    FileHandler.fileDelete(report.getItems().get(lastItemId).getPicture().getFilePath());
//                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == PICTURE_VIEWER_SCREEN_ID){
                Item newItem = (Item) data.getSerializableExtra(ITEM_KEY);
                int itemId = data.getIntExtra(ITEM_ID_KEY, -1);
                if(itemId > -1) {
                    if(oldItemPicturePath == null) {
                        oldItemPicturePath = report.getItems().get(itemId).getPicture().getFilePath();
                    } else {
                        writeData("Trying to replace an old report item's picture", getExternalFilesDir(null));
                        FileHandler.fileDelete(report.getItems().get(itemId).getPicture().getFilePath());
                    }
                    lastItemId = itemId;
                    report.getItems().set(itemId, newItem);
                }
                itemAdapter.notifyDataSetChanged();
                save();
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if(canEdit) {
            super.onListItemClick(l, v, position, id);
        }
    }

    public void updatePendingItemsCount(){
        int pendingItems = report.getPendingItemsCount();
        ((TextView)findViewById(R.id.tvPendingItems)).setText(
                String.format(getResources().getConfiguration().locale, "%s: %d",
                        getResources().getString(R.string.pendingItems),
                        pendingItems));
    }

    private void toggleControls(boolean bResume){
        canEdit = bResume;
        getListView().setClickable(bResume);
        getListView().setEnabled(bResume);
        if(bResume){
            findViewById(R.id.pbItemReportEdit).setVisibility(View.INVISIBLE);
        }
        else{
            findViewById(R.id.pbItemReportEdit).setVisibility(View.VISIBLE);
        }
    }

    private void save(){
        toggleControls(false);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_REPORTITEMSSAVE_KEY);
        uriBuilder.setReport(report);
        uriBuilder.setProject(report.getDrawingref().getProject());
        restTemplateHelper.execute(uriBuilder);
    }

    @Override
    public void onChangeHappened(Item item, int position, View view) {
        if(view instanceof ImageView) {
            Intent intent = new Intent(ItemReportEditActivity.this, PictureViewerActivity.class);
            intent.putExtra(ITEM_KEY, item);
            intent.putExtra(ITEM_ID_KEY, position);
            intent.putExtra(REPORT_KEY, report);
            intent.putExtra(PROJECT_KEY, project);
            startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
        } else {
            save();
        }
    }

    private void exit(boolean error){
        if(error) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finish();
        finishAndRemoveTask();
        stopLockTask();
        super.onDestroy();
    }

    private void dataLoadError(){
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getResources().getString(R.string.dataRecoverError);
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exit(true);
            }
        });
        builder.show();
    }

    private void dataLoadError(String customMessage){
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(customMessage);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private void projectLoad(){
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_QRPROJECTLOAD_KEY);
        uriBuilder.getParameters().add(StringHandler.createQrText(report.getDrawingref().getProject()));
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        if(result != null) {
            if (!result.equals(App.getContext().getResources().getString(R.string.drawingLockedMessage))) {
                if (requestCode.equals(REST_REPORTITEMSSAVE_KEY)){
                    updatePendingItemsCount();
                    toggleControls(true);
                }else if (requestCode.equals(REST_QRPROJECTLOAD_KEY)) {
                    Project project = JsonHandler.toProject(result);
                    report = (ItemReport) project.getDrawingRefs().get(report.getDrawingref().getId()).getReports().get(report.getId());
                    toggleControls(true);
                }
            } else {
                toggleControls(false);
                projectLoad();
                dataLoadError(App.getContext().getResources().getString(R.string.drawingLockedMessage));
            }
        } else {
            dataLoadError();
        }
    }
}
