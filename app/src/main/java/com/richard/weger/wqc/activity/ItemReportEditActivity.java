package com.richard.weger.wqc.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.firebird.FirebirdMessagingService;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.adapter.ItemAdapter;
import com.richard.weger.wqc.helper.JsonHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.ReportHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.richard.weger.wqc.constants.AppConstants.*;
import static com.richard.weger.wqc.helper.LogHelper.writeData;

public class ItemReportEditActivity extends ListActivity implements ItemAdapter.ChangeListener, RestTemplateHelper.RestHelperResponse, FirebirdMessagingService.FirebaseListener {

    ItemReport report;
    Project project;
    ItemAdapter itemAdapter;
    String oldItemPicturePath;
    int lastItemId = -1;
    boolean canEdit = true;

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
    public void onBackPressed() {
        /*if (!shouldAllowBack()) {
            doSomething();
        } else {
            super.onBackPressed();
        }
        */
    }

    @Override
    protected void onResume(){
        super.onResume();
        FirebirdMessagingService.delegate = this;
        setRunnable();
    }

    @Override
    protected void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
//        close(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        writeData("Started intent data get");
        Intent intent = getIntent();
        if(intent != null) {
            int id;
            id = intent.getIntExtra(REPORT_ID_KEY, -1);
            if(id < 0){
                writeData("Invalid id got from intent. Aborting");
                setResult(RESULT_CANCELED);
                finish();
            }
            writeData("Started project from intent extras get");
            project = (Project) intent.getSerializableExtra(PROJECT_KEY);
            writeData("Started project references link");
            ProjectHelper.linkReferences(project);
            writeData("Started report get from project");
            report = (ItemReport) project.getDrawingRefs().get(0).getReports().get(id);
        }
        if(report == null || project == null){
            writeData("Invalid project and / or report. Closing report edit screen.");
            Toast.makeText(this, R.string.dataRecoverError,Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inflateActivityLayout();
    }

    private void inflateActivityLayout(){
        setContentView(R.layout.activity_item_report_edit);
        writeData("Setting item adapter");
        itemAdapter = new ItemAdapter(this, report.getItems(), project);
        setListAdapter(itemAdapter);
        itemAdapter.setChangeListener(this);
        setListeners();
        setTextViews();
        toggleControls(true);
    }

    private void setTextViews(){
        writeData("Setting text views");
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
        writeData("Setting listeners");
        Button btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String currentItemPicturePath = report.getItems().get(lastItemId).getPicture().getFileName();
//                if(oldItemPicturePath != null
//                        &&
//                        !currentItemPicturePath.contains(oldItemPicturePath)){
//                    FileHelper.fileDelete(report.getItems().get(lastItemId).getPicture().getFileName());
//                }
                close(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        writeData("Started activity result handling");
        if(resultCode == RESULT_OK){
            writeData("Result code is OK");
            if(requestCode == PICTURE_VIEWER_SCREEN_ID){
                writeData("Result came from picture viewer screen");
                Item newItem = (Item) data.getSerializableExtra(ITEM_KEY);
                int itemId = data.getIntExtra(ITEM_ID_KEY, -1);
                if(itemId > -1) {
                    lastItemId = itemId;
                    report.getItems().set(itemId, newItem);
                    itemAdapter.notifyDataSetChanged();
                    save(report.getItems().get(itemId), true);
                }
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if(canEdit) {
            writeData("Report list item clicked");
            super.onListItemClick(l, v, position, id);
        }
    }

    public void updatePendingItemsCount(){
        writeData("Updating pending items count");
        int pendingItems = report.getPendingItemsCount();
        ((TextView)findViewById(R.id.tvPendingItems)).setText(
                String.format(getResources().getConfiguration().locale, "%s: %d",
                        getResources().getString(R.string.pendingItems),
                        pendingItems));
    }

    private void toggleControls(boolean bResume){
        writeData("Toggling screen controls");
        canEdit = bResume;
        itemAdapter.setEnabled(bResume);
        itemAdapter.notifyDataSetChanged();
        getListView().setClickable(bResume);
        getListView().setEnabled(bResume);
        if(bResume){
            findViewById(R.id.pbItemReportEdit).setVisibility(View.INVISIBLE);
        }
        else{
            findViewById(R.id.pbItemReportEdit).setVisibility(View.VISIBLE);
        }
    }

    private void save(Item item, boolean savePicture){
        writeData("Started report save request");
        toggleControls(false);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_ITEMSAVE_KEY);
        uriBuilder.setReport(report);
        uriBuilder.setItem(item);
        uriBuilder.setProject(report.getDrawingref().getProject());
        restTemplateHelper.execute(uriBuilder);

        String fileName = StringHelper.getPicturesFolderPath(project).concat("/").concat(item.getPicture().getFileName());
        if(savePicture && FileHelper.isValidFile(fileName)) {
            pictureUpload(item);
        }
    }

    private void pictureUpload(Item item){
        writeData("Started picture upload request");
        toggleControls(false);

        String picName = item.getPicture().getFileName();
        picName = picName.substring(picName.lastIndexOf("/") + 1);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PICTUREUPLOAD_KEY);
        uriBuilder.setReport(report);
        uriBuilder.setItem(item);
        uriBuilder.setProject(report.getDrawingref().getProject());
        uriBuilder.getParameters().add(picName);
        restTemplateHelper.execute(uriBuilder);
    }

    @Override
    public void onChangeHappened(int position, View view) {
        writeData("Started list's item change handler");
        Item item = report.getItems().get(position);
        if(view instanceof ImageView) {
            Intent intent = new Intent(ItemReportEditActivity.this, PictureViewerActivity.class);
            intent.putExtra(ITEM_KEY, item);
            intent.putExtra(ITEM_ID_KEY, position);
            intent.putExtra(REPORT_KEY, report);
            intent.putExtra(PROJECT_KEY, project);
            startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
        } else {
            save(item, false);
        }
    }

    private void close(boolean error){
        if(error) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finish();
        finishAndRemoveTask();
        writeData("Closing item report edit activity");
        stopLockTask();
        super.onDestroy();
    }

    private void dataLoadError(){
        writeData("Showing data load error default message");
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getResources().getString(R.string.dataRecoverError);
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

    private void dataLoadError(String customMessage){
        writeData("Showing data load error custom message");
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
        writeData("Started routine to load project from the server");
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_QRPROJECTLOAD_KEY);
        uriBuilder.getParameters().add(StringHelper.getQrText(report.getDrawingref().getProject()));
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        writeData("Started server response handler");
        if(result != null) {
            writeData("response is not null");
            if (!result.equals(App.getContext().getResources().getString(R.string.drawingLockedMessage))) {
                if (requestCode.equals(REST_REPORTITEMSSAVE_KEY)) {
                    writeData("The response was got from a report item save request");
                    updatePendingItemsCount();
                    toggleControls(true);
                } else if (requestCode.equals(REST_ITEMSAVE_KEY)) {
                    writeData("The response was got from an item save request");
                    Item item;
                    item = JsonHelper.toObject(result, Item.class);
                    if (item != null) {
                        for (int i = 0; i < report.getItems().size(); i++) {
                            if (report.getItems().get(i).getId() == item.getId()) {
                                report.getItems().set(i, item);
                                break;
                            }
                        }
                        updatePendingItemsCount();
                        toggleControls(true);
                    } else {
                        dataLoadError();
                    }
                } else if (requestCode.equals(REST_PICTUREDOWNLOAD_KEY)){
                    inflateActivityLayout();
                    toggleControls(true);
                } else if (requestCode.equals(REST_PICTUREUPLOAD_KEY)){
                    writeData("The response was got from a picture save request");
                    updatePendingItemsCount();
                    toggleControls(true);
                } else if (requestCode.equals(REST_QRPROJECTLOAD_KEY)) {
                    writeData("The response was got from a project load request");
                    Project project = ProjectHelper.fromJson(result);
                    report = (ItemReport) project.getDrawingRefs().get(0).getReport(report.getId());
                    itemAdapter.setItemList(report.getItems());
                    itemAdapter.notifyDataSetChanged();
                    toggleControls(true);
                }
            } else {
                if(requestCode.equals(REST_PICTUREUPLOAD_KEY)) {
                    toggleControls(false);
                    int id = Integer.valueOf(result.substring(result.lastIndexOf("#") + 1));

                    Item item = report.getItems().get(id);
                    List<Item> items = new ArrayList<>();
                    items.add(item);

                    FileHelper.fileDelete(StringHelper.getPictureFilePath(project, item));

                    ReportHelper reportHelper = new ReportHelper();
                    reportHelper.getPictures(this, items, null);
                } else {
                    writeData("A write attempt was made but the drawing is currently locked by another user. Aborting write attempt");
                    toggleControls(false);
                    projectLoad();
                    dataLoadError(App.getContext().getResources().getString(R.string.drawingLockedMessage));
                }
            }
        } else {
            dataLoadError();
        }
    }

    @Override
    public void messageReceived(Map<String, String> data) {
        String qrCode = data.get("qrCode");
        if(StringHelper.getQrText(report.getDrawingref().getProject()).equals(qrCode)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    projectLoad();
                }
            });
        }
    }
}
