package com.richard.weger.wqc.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.ItemAdapter;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.messaging.IMessagingListener;
import com.richard.weger.wqc.messaging.MessagingHelper;
import com.richard.weger.wqc.messaging.firebird.FirebaseHelper;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.service.ItemRequestParametersResolver;
import com.richard.weger.wqc.service.ReportRequestParametersResolver;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.LoggerManager;

import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_ID_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_NOT_CHECKED_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_PICTURE_MODE;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_CAPTURE_MODE;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_VIEWER_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_ID_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_CONFIGLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_ITEMSAVE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_REPORTUPLOAD_KEY;

public class ItemReportEditActivity extends ListActivity implements ItemAdapter.ChangeListener,
        RestTemplateHelper.RestResponseHandler,
        IMessagingListener {

    ItemReport report;
    Project project;
    Long reportId;
    ItemAdapter itemAdapter;
    ParamConfigurations conf;
    boolean canEdit = true;
    Parcelable state;
    boolean paused = false;
    Logger logger;
    boolean onCreateChain = false;

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

        logger = LoggerManager.getLogger(getClass());

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

    private void inflateActivityLayout(){
        setContentView(R.layout.activity_item_report_edit);
        logger.info("Setting item adapter");
        itemAdapter = new ItemAdapter(this, report.getItems(), project);
        setListAdapter(itemAdapter);
        itemAdapter.setChangeListener(this);
        setListeners();
        setTextViews();
        toggleControls(true);

        if(DeviceHelper.isOnlyRole("te")){
            itemAdapter.setEnabled(false);
            (findViewById(R.id.chkFinished)).setEnabled(false);
            (findViewById(R.id.chkFinished)).setClickable(false);
        } else {
            itemAdapter.setEnabled(!report.isFinished());
            (findViewById(R.id.chkFinished)).setEnabled(true);
            (findViewById(R.id.chkFinished)).setClickable(true);
        }
        itemAdapter.setCameraEnabled(true);
        itemAdapter.notifyDataSetChanged();

    }

    private void setTextViews(){
        logger.info("Setting text views");
        ((TextView)findViewById(R.id.tvProjectInfo)).setText(
                String.format(getResources().getConfiguration().getLocales().get(0),
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
        ImageButton btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(v -> close(false));

        CheckBox chkFinished = findViewById(R.id.chkFinished);
        chkFinished.setOnClickListener((view) ->
        {
            toggleControls(false);
            if(report.getItems().stream().noneMatch(i -> i.getStatus() == ITEM_NOT_CHECKED_KEY) || report.isFinished()) {
                shouldChangeReportState();
            } else {
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_UNFINISHED_ITEMREPORT_WARNING, getResources().getString(R.string.pendingItemsMessage), ErrorResult.ErrorLevel.WARNING, getClass());
                ErrorResponseHandler.handle(err, this, this::cancelReportFinish);
            }
        });

    }

    private void shouldChangeReportState(){
        if(!report.isFinished()) {
            AlertHelper.showMessage(this,
                    getResources().getString(R.string.confirmationNeeded),
                    getResources().getString(R.string.reportFinishMessage),
                    getResources().getString(R.string.yesTAG),
                    getResources().getString(R.string.noTag),
                    () -> reportFinish(true),
                    this::cancelReportFinish);
        } else {
            AlertHelper.showMessage(this,
                    getResources().getString(R.string.confirmationNeeded),
                    getResources().getString(R.string.reportUnfinishMessage),
                    getResources().getString(R.string.yesTAG),
                    getResources().getString(R.string.noTag),
                    () -> reportFinish(false),
                    this::cancelReportFinish);
        }
    }

    private void cancelReportFinish(){
        toggleControls(true);
        ((CheckBox) findViewById(R.id.chkFinished)).setChecked(report.isFinished());
    }

    private void reportFinish(boolean finish){
        toggleControls(false);
        if(report.getClient() == null || report.getClient().isEmpty()){
            AlertHelper.getString(this, getResources().getString(R.string.clientNameInformMessage), (content) -> {
                if(content != null && !content.isEmpty()) {
                    report.setClient(content);
                    reportFinish(!report.isFinished());
                } else {
                    ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_EMPTY_FIELDS_WARNING, getResources().getString(R.string.emptyFieldsError), ErrorResult.ErrorLevel.WARNING, getClass());
                    ErrorResponseHandler.handle(err, this, () -> toggleControls(true));
                }
            });
        } else {
            report.setFinished(finish);
            ReportRequestParametersResolver resolver = new ReportRequestParametersResolver(REST_REPORTUPLOAD_KEY, conf,false);
            resolver.postEntity(report, this);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if(canEdit) {
            super.onListItemClick(l, v, position, id);
        }
    }

    public void updatePendingItemsCount(){
        logger.info("Updating pending items count");
        long pendingItems = report.getItems().stream().filter(i -> i.getStatus() == ITEM_NOT_CHECKED_KEY).count();
        ((TextView)findViewById(R.id.tvPendingItems)).setText(
                String.format(getResources().getConfiguration().getLocales().get(0), "%s: %d",
                        getResources().getString(R.string.pendingItemsLabel),
                        pendingItems));
    }

    @Override
    public void toggleControls(boolean bResume){
        logger.info("Toggling screen controls");
        canEdit = bResume;
        itemAdapter.setEnabled(bResume && !report.isFinished() && !DeviceHelper.isOnlyRole("te"));
        itemAdapter.notifyDataSetChanged();
        if(bResume){
            findViewById(R.id.pbItemReportEdit).setVisibility(View.INVISIBLE);
        }
        else{
            findViewById(R.id.pbItemReportEdit).setVisibility(View.VISIBLE);
        }
        (findViewById(R.id.chkFinished)).setEnabled(bResume && !DeviceHelper.isOnlyRole("te"));
        ((CheckBox) findViewById(R.id.chkFinished)).setChecked(report.isFinished());
        itemAdapter.setCameraEnabled(true);
    }

    @Override
    public void onFatalError() {

    }

    private void storeCurrListPosition(){
        ListView list = findViewById(android.R.id.list);
        state = list.onSaveInstanceState();
    }

    private void restoreCurrListPosition(){
        ListView list = findViewById(android.R.id.list);
        list.onRestoreInstanceState(state);
    }

    private void save(Item item, boolean savePicture){
        logger.info("Started ReportItem save request");
        toggleControls(false);

        String fileName = StringHelper.getPicturesFolderPath(project).concat("/").concat(item.getPicture().getFileName());

        ItemRequestParametersResolver resolver = new ItemRequestParametersResolver(REST_ITEMSAVE_KEY, conf, false);
        resolver.postEntity(item, this);

        if(savePicture && FileHelper.isValidFile(fileName)) {
            ProjectHelper.itemPictureUpload(this, item, project);
        }

    }

    @Override
    public void onChangeHappened(int position, View view) {
        logger.info("Started list's item change handler");
        toggleControls(false);
        Item item = report.getItems().get(position);
        if(view instanceof ImageView) {
            storeCurrListPosition();
            paused = true;
            view.setEnabled(false);
            view.setClickable(false);
            Intent intent = new Intent(ItemReportEditActivity.this, PictureViewerActivity.class);
            intent.putExtra(ITEM_KEY, item);
            intent.putExtra(ITEM_ID_KEY, position);
            intent.putExtra(PICTURE_CAPTURE_MODE, ITEM_PICTURE_MODE);
            intent.putExtra(REPORT_KEY, report);
            intent.putExtra(PROJECT_KEY, project);
            startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
        } else {
            save(item, false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.info("Started activity result handling");
        toggleControls(false);
        if(resultCode == RESULT_OK && requestCode == PICTURE_VIEWER_SCREEN_ID){
            Item newItem = (Item) data.getSerializableExtra(ITEM_KEY);
            if(newItem != null) {
                save(newItem, true);
            }
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
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    logger.info("The response was got from a project load request");
                    project = ResultService.getSingleResult(result, Project.class);
                    report = (ItemReport) project.getDrawingRefs().get(0).getReports().stream().
                            filter(r -> r.getId().equals(reportId))
                            .findFirst()
                            .orElse(null);
                    if (report == null) {
                        ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.INVALID_ENTITY, getResources().getString(R.string.unknownErrorMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
                        ErrorResponseHandler.handle(err, this, () -> close(true));
                        return;
                    }
                    inflateActivityLayout();
                    itemAdapter.setItemList(report.getItems());
                    itemAdapter.notifyDataSetChanged();
                    restoreCurrListPosition();
                    break;
                case REST_REPORTUPLOAD_KEY:
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
                    if(!onCreateChain) {
                        MessagingHelper.getServiceInstance().setListener(this, true);
                    } else {
                        onCreateChain = false;
                        MessagingHelper.getServiceInstance().setup(this);
                    }
                    break;
            }
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, () -> close(true));
        }
    }

}
