package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.ItemReportAdapter;
import com.richard.weger.wqc.adapter.ReportItemActionHandler;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ImageHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.messaging.IMessagingListener;
import com.richard.weger.wqc.messaging.MessagingHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.service.ItemRequestParametersResolver;
import com.richard.weger.wqc.service.ReportRequestParametersResolver;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.LoggerManager;

import java.util.ArrayList;
import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_NOT_CHECKED_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURES_LIST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_START_INDEX_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_VIEWER_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_ID_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REQUEST_IMAGE_CAPTURE_ACTION;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_CONFIGLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_ITEMSAVE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_REPORTUPLOAD_KEY;
import static com.richard.weger.wqc.util.App.getStringResource;

public class ItemReportEditActivity extends Activity implements ReportItemActionHandler,
        RestTemplateHelper.RestResponseHandler,
        IMessagingListener {

    ItemReport report;
    Project project;
    Long reportId;
    ParamConfigurations conf;
    boolean canEdit = true;
    Parcelable state;
    boolean paused = false;
    Logger logger;
    boolean onCreateChain = false;
    Item itemToUpdadePicture;
    String tempPicPath;
    private ItemReportAdapter adapter;
    boolean isWaiting = false;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume(){
        super.onResume();
        if(!paused) {
            ConfigurationsManager.loadServerConfig(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!paused) {
            storeCurrListPosition();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onCreateChain = true;

        logger = LoggerManager.getLogger(ItemReportEditActivity.class);

        logger.info("Started intent data get");
        Intent intent = getIntent();
        if(intent != null) {
            reportId = intent.getLongExtra(REPORT_ID_KEY, -1);
            if(reportId < 0){
                logger.severe("Invalid id got from intent. Aborting");
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private void updateRecyclerView(){
        RecyclerView view = findViewById(R.id.recyclerview);
        if(adapter == null){
            adapter = new ItemReportAdapter(StringHelper.getPicturesFolderPath(project),report.getItems(), this);
        }
        GridLayoutManager grid = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        view.setLayoutManager(grid);
        view.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private void inflateActivityLayout(){
        setContentView(R.layout.activity_item_report_edit);
        logger.info("Setting item adapter");

        updateRecyclerView();


        setListeners();
        setTextViews();
        toggleControls(true);

        if(DeviceHelper.isOnlyRole("te")){
            adapter.setEnabled(false);
            (findViewById(R.id.chkFinished)).setEnabled(false);
            (findViewById(R.id.chkFinished)).setClickable(false);
        } else {
            adapter.setEnabled(!report.isFinished());
            (findViewById(R.id.chkFinished)).setEnabled(true);
            (findViewById(R.id.chkFinished)).setClickable(true);
        }
        adapter.notifyDataSetChanged();

    }

    private void setTextViews(){
        logger.info("Setting text views");
        ((TextView)findViewById(R.id.tvProjectInfo)).setText(
                String.format(App.getLocale(),
                        "%s - Z%d",
                        project.getReference(),
                        project.getDrawingRefs().get(0).getDnumber()
//                        ,project.getDrawingRefs().getContext(0).getPart().getContext(0).getNumber()
                        )
        );
        ((TextView)findViewById(R.id.tvReportType)).setText(report.toString());


        updatePendingItemsCount();
    }

    private void setListeners(){
        logger.info("Setting listeners");
        ImageButton btn = findViewById(R.id.backButton);
        btn.setOnClickListener(v -> close(false));

        CheckBox chkFinished = findViewById(R.id.chkFinished);
        chkFinished.setOnClickListener((view) ->
        {
            toggleControls(false);
            if(report.getItems().stream().noneMatch(i -> i.getStatus() == ITEM_NOT_CHECKED_KEY) || report.isFinished()) {
                shouldChangeReportState();
            } else {
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_UNFINISHED_ITEMREPORT_WARNING, getStringResource(R.string.pendingItemsMessage), ErrorResult.ErrorLevel.WARNING);
                ErrorResponseHandler.handle(err, this::cancelReportFinish);
            }
        });

    }

    private void shouldChangeReportState(){
        if(!report.isFinished()) {
            AlertHelper.showMessage(getStringResource(R.string.confirmationNeeded),
                    getStringResource(R.string.reportFinishMessage),
                    getStringResource(R.string.yesTAG),
                    getStringResource(R.string.noTag),
                    () -> reportFinish(true),
                    this::cancelReportFinish, this);
        } else {
            AlertHelper.showMessage(
                    getStringResource(R.string.confirmationNeeded),
                    getStringResource(R.string.reportUnfinishMessage),
                    getStringResource(R.string.yesTAG),
                    getStringResource(R.string.noTag),
                    () -> reportFinish(false),
                    this::cancelReportFinish, this);
        }
    }

    private void cancelReportFinish(){
        toggleControls(true);
        ((CheckBox) findViewById(R.id.chkFinished)).setChecked(report.isFinished());
    }

    private void reportFinish(boolean finish){
        toggleControls(false);
        if(report.getClient() == null || report.getClient().isEmpty()){
            AlertHelper.getString(getStringResource(R.string.clientNameInformMessage), (content) -> {
                if(content != null && !content.isEmpty()) {
                    report.setClient(content);
                    reportFinish(!report.isFinished());
                } else {
                    ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_EMPTY_FIELDS_WARNING, getStringResource(R.string.emptyFieldsError), ErrorResult.ErrorLevel.WARNING);
                    ErrorResponseHandler.handle(err, () -> toggleControls(true));
                }
            });
        } else {
            report.setFinished(finish);
            ReportRequestParametersResolver resolver = new ReportRequestParametersResolver(REST_REPORTUPLOAD_KEY, conf,false);
            resolver.postEntity(report, this);
        }
    }

    public void updatePendingItemsCount(){
        logger.info("Updating pending items count");
        long pendingItems = report.getItems().stream().filter(i -> i.getStatus() == ITEM_NOT_CHECKED_KEY).count();
        ((TextView)findViewById(R.id.tvPendingItems)).setText(
                String.format(App.getLocale(), "%s: %d",
                        getStringResource(R.string.pendingItemsLabel),
                        pendingItems));
    }

    @Override
    public void toggleControls(boolean bResume){
        logger.info("Toggling screen controls");
        canEdit = bResume;

        if(adapter != null) {
            adapter.setEnabled(bResume && !report.isFinished() && !DeviceHelper.isOnlyRole("te"));
            adapter.notifyDataSetChanged();
        }

        ProgressBar pb = findViewById(R.id.pbItemReportEdit);
        if (pb != null) {
            if (bResume) {
                pb.setVisibility(View.INVISIBLE);
            } else {
                pb.setVisibility(View.VISIBLE);
            }
        }
        CheckBox chk = (findViewById(R.id.chkFinished));
        if(chk != null) {
            chk.setEnabled(bResume && !DeviceHelper.isOnlyRole("te"));
            chk.setChecked(report.isFinished());
        }

    }

    @Override
    public void onFatalError() {

    }

    private void storeCurrListPosition(){
        ListView list = findViewById(android.R.id.list);
        if(list != null) {
            state = list.onSaveInstanceState();
        }
    }

    private void restoreCurrListPosition(){
        ListView list = findViewById(android.R.id.list);
        if(list != null) {
            list.onRestoreInstanceState(state);
        }
    }

    private void save(Item item, boolean savePicture){
        if(!isWaiting) {
            logger.info("Started ReportItem save request");
            isWaiting = true;
            toggleControls(false);

            String fileName = StringHelper.getPicturesFolderPath(project).concat("/").concat(item.getPicture().getFileName());

            storeCurrListPosition();
            ItemRequestParametersResolver resolver = new ItemRequestParametersResolver(REST_ITEMSAVE_KEY, conf, false);
            resolver.postEntity(item, this);

            if (savePicture && FileHelper.isValidFile(fileName)) {
                ProjectHelper.itemPictureUpload(this, item, project);
            }
        }

    }

    private boolean isEditable() {
        return !report.isFinished() && !DeviceHelper.isOnlyRole("te");
    }

    @Override
    public void onPictureTap(int position) {
        logger.info("Started list's item change handler");
        toggleControls(false);
        Item item = report.getItems().get(position);
        storeCurrListPosition();
        paused = true;
        if (FileHelper.isValidFile(StringHelper.getPicturesFolderPath(project) + item.getPicture().getFileName())) {
            Intent intent = new Intent(getApplicationContext(), PictureViewerActivity.class);
            intent.putStringArrayListExtra(PICTURES_LIST_KEY, new ArrayList<String>() {{
                add(item.getPicture().getFileName());
            }});
            intent.putExtra(PICTURE_START_INDEX_KEY, position);
            startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
            return;
        } else if (isEditable()) {
            takePicture(item);
            return;
        }
        toggleControls(true);
    }

    @Override
    public void onCommentsChange(int position, String newContent) {
        logger.info("Started comments change request handlerr");
        toggleControls(false);
        Item item = report.getItems().get(position);
        if(isEditable() && !newContent.equals(item.getComments())) {
            item.setComments(newContent);
            save(item, false);
        } else {
            toggleControls(true);
        }
    }

    @Override
    public void onStatusTap(int value, int position) {
        Item item = report.getItems().get(position);
        if(item.getStatus() != value && isEditable()) {
            logger.info("Started status change request handler");
            toggleControls(false);
            item.setStatus(value);
            save(item, false);
        }
    }

    @Override
    public void onRequestPictureCapture(int position) {
        logger.info("Started picture capture request handler");
        toggleControls(false);
        if(isEditable()) {
            Item item = report.getItems().get(position);
            takePicture(item);
        }
    }

    private void takePicture(Item item) {
        itemToUpdadePicture = item;
        tempPicPath = ImageHelper.takePicture(this, project, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.info("Started activity result handling");
        toggleControls(false);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE_ACTION) {
            String picPath = tempPicPath;
            picPath = ImageHelper.compressImage(picPath);
            ImageHelper.putPicInfoAndTimestamp(picPath, project, ImageHelper.getPrefixedPicNumber(itemToUpdadePicture));
            save(itemToUpdadePicture, true);
        } else {
            restoreCurrListPosition();
            toggleControls(true);
        }
    }

    private void close(boolean error){
        MessagingHelper.getServiceInstance().removeListener();
        if(error) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finishAndRemoveTask();
        logger.info("Closing item report edit activity");
        stopLockTask();
        super.onDestroy();
    }

    @Override
    public boolean shouldNotifyChange(String qrCode, Long id, Long parentId) {
        try{
            runOnUiThread(()-> toggleControls(false));
        } catch (Exception e){
            logger.warning("Unable to disable controls for project refresh routine!");
        }
        if(!paused) {
            storeCurrListPosition();
        } else {
            paused = false;
        }
        if(ProjectHelper.shouldRefresh(report, id, parentId)) {
            logger.info("The current report was updated by another user. Triggering reload from ItemReport activity");
            ProjectHelper.projectLoad(this, false);
            return true;
        }
        logger.info("Another report was updated by another user. There is no need to refresh the contents.");
        runOnUiThread(()-> toggleControls(true));
        return false;
    }


    @Override
    public void onConnectionSuccess() {
        ProjectHelper.projectLoad(this, report == null);
    }

    @Override
    public void onConnectionFailure() {
        close(true);
    }

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        logger.info("Started server response handler");
        if(result instanceof SuccessResult){
            switch (result.getRequestCode()) {
                case REST_ITEMSAVE_KEY:
                case REST_REPORTUPLOAD_KEY:
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    logger.info("The response was got from a project load request");
                    project = ResultService.getSingleResult(result, Project.class);
                    report = (ItemReport) project.getDrawingRefs().get(0).getReports().stream().
                            filter(r -> r.getId().equals(reportId))
                            .findFirst()
                            .orElse(null);
                    if (report == null) {
                        ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.INVALID_ENTITY, getStringResource(R.string.unknownErrorMessage), ErrorResult.ErrorLevel.SEVERE);
                        ErrorResponseHandler.handle(err, () -> close(true));
                        return;
                    }
                    inflateActivityLayout();
                    adapter.setItemList(report.getItems());
                    adapter.notifyDataSetChanged();
                    restoreCurrListPosition();
                    isWaiting = false;
                    break;
                case REST_PICTUREDOWNLOAD_KEY:
                    toggleControls(true);
                    Toast.makeText(this, R.string.changesSavedMessage, Toast.LENGTH_SHORT).show();
                    break;
                case REST_PICTUREUPLOAD_KEY:
                    logger.info("The response was got from a picture save request");
                    updatePendingItemsCount();
                    ProjectHelper.projectLoad(this);
                    Toast.makeText(this, R.string.changesSavedMessage, Toast.LENGTH_SHORT).show();
                    break;
                case REST_CONFIGLOAD_KEY:
                    ParamConfigurations c = ResultService.getSingleResult(result, ParamConfigurations.class);
                    ConfigurationsManager.setServerConfig(c);
                    conf = c;
                    MessagingHelper.getServiceInstance().setListener(this, true);
                    break;
            }
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, () -> close(true));
        }
    }

}
