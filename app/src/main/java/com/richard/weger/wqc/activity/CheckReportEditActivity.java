package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.firebird.FirebirdMessagingService;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.MessageboxHelper;
import com.richard.weger.wqc.helper.PdfHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.helper.WQCDocumentHelper;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.service.MarkRequestParametersResolver;
import com.richard.weger.wqc.service.ProjectRequestParametersResolver;
import com.richard.weger.wqc.service.ReportRequestParametersResolver;
import com.richard.weger.wqc.util.LoggerManager;
import com.richard.weger.wqc.util.TouchImageView;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.PARAMCONFIG_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_ID_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_MARKREMOVE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_MARKSAVE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_REPORTUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.SDF;

public class CheckReportEditActivity extends Activity implements TouchImageView.ChangeListener,
        EntityRestTemplateHelper.RestTemplateResponse,
        FirebirdMessagingService.FirebaseListener {

    CheckReport report;
    ParamConfigurations conf;
    Bitmap originalBitmap = null,
            currentBitmap = null;
    int currentPage = 0,
        pageCount = 0;
    TouchImageView imageView = null;
    String filePath;
    Mark lastTouchedMark;
    int mode = 0;
    // mode 0 = zoom / pan
    // mode 1 = add mark

    Runnable runnable;
    Handler handler = new Handler();
    boolean paused = false;
    boolean canRun = false;

    Logger logger;

    private void setRunnable(){
        final int interval = 1000;
        if(canRun) {
            if(runnable == null) {
                runnable = () -> {
                    if (!checkInternetConnection()) {
                        setWaitingLayout();
                        paused = true;
                    } else {
                        inflateActivityLayout();
                        if (paused) {
                            projectLoad();
                            paused = false;
                        }
                    }
                };
                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);
            }
        }
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
    public void onBackPressed(){

    }

    @Override
    protected void onPause(){
        super.onPause();
        close(false);
        if(runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        FirebirdMessagingService.delegate = this;
        setRunnable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = LoggerManager.getLogger(getClass());

        String folder;

        Intent intent = getIntent();
        Long id;
        id = intent.getLongExtra(REPORT_ID_KEY, -1);
        if(id < 0){
            setResult(RESULT_CANCELED);
            finish();
        }
        conf = (ParamConfigurations) intent.getSerializableExtra(PARAMCONFIG_KEY);
        Project project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        ProjectHelper.linkReferences(project);
        report = (CheckReport) project.getDrawingRefs().get(0).getReports().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        folder = StringHelper.getPdfsFolderPath(project);

        filePath = folder.concat(File.separator).concat(report.getFileName());

        if(!FileHelper.isValidFile(filePath)){
            setWaitingLayout();
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_FILE_ACCESS_EXCEPTION, getResources().getString(R.string.fileNotFoundMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
            ErrorResponseHandler.handle(err, this, () ->{
                setResult(RESULT_OK);
                finish();
            });
        } else {
            canRun = true;
            inflateActivityLayout();
            if(runnable == null){
                setRunnable();
            }
        }
    }

    private void configImageView(){
        logger.info("Started image view config");
        imageView = findViewById(R.id.ivDocument);
        imageView.setMaxZoom(12f);
        imageView.setChangeListener(this);
        imageView.setTag(mode);

        pageCount = PdfHelper.getPageCount(filePath);
        if(pageCount == 0 || pageCount != report.getPages().size()){
            setResult(RESULT_CANCELED);
            finish();
        }

        originalBitmap = WQCDocumentHelper.pageLoad(currentPage, filePath, getResources());

        if(originalBitmap != null) {
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
            updateButtonState();
            setListeners();
            updatePointsDrawing();
        } else {
            String message = getResources().getString(R.string.reportPageLoadFailed).concat("\n(").concat(getResources().getString(R.string.invalidEntityError));
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_REPORT_PAGE_LOAD_EXCEPTION, message, ErrorResult.ErrorLevel.SEVERE, getClass());
            ErrorResponseHandler.handle(err, this, null);
        }
    }

    @SuppressWarnings("unchecked")
    private void inflateActivityLayout(){
        logger.info("Setting content view");
        setContentView(R.layout.activity_check_report_edit);

        if(DeviceHelper.isOnlyRole("TE") || report.isFinished()){
            findViewById(R.id.btnAddMark).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnUndo).setVisibility(View.INVISIBLE);
            (findViewById(R.id.cmbRole)).setVisibility(View.INVISIBLE);
        }

        if(DeviceHelper.isOnlyRole("TE")){
            (findViewById(R.id.chkFinished)).setEnabled(false);
            (findViewById(R.id.chkFinished)).setClickable(false);
        }

        Spinner cmbRoles = findViewById(R.id.cmbRole);
        ArrayAdapter rolesAdapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Objects.requireNonNull(DeviceHelper.getCurrentDevice().getRoles().stream().map(Role::getDescription).toArray()));
        cmbRoles.setAdapter(rolesAdapter);

        configImageView();
    }

    private void setListeners(){
        logger.info("Setting buttons listeners");
        Button btn = findViewById(R.id.btnNext);
        btn.setOnClickListener(view -> nextPage());

        btn = findViewById(R.id.btnPrevious);
        btn.setOnClickListener(view -> previousPage());

        btn = findViewById(R.id.btnAddMark);
        btn.setOnClickListener(view -> toggleMarkAdd());

        btn = findViewById(R.id.btnUndo);
        btn.setOnClickListener(view -> undo());

        btn = findViewById(R.id.btnExit);
        btn.setOnClickListener(view -> close(false));

        CheckBox chkFinished = findViewById(R.id.chkFinished);
        chkFinished.setChecked(report.isFinished());
        chkFinished.setOnClickListener((view) ->
        {
            toggleControls(false);
            if(report.getMarksCount() > 0 || report.isFinished()) {
                shouldChangeReportState();
            } else {
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_UNFINISHED_CHECKREPORT_WARNING, getResources().getString(R.string.noMarksMessage), ErrorResult.ErrorLevel.WARNING, getClass());
                ErrorResponseHandler.handle(err, this, this::cancelReportFinish);
            }
        });
    }

    private void shouldChangeReportState(){
        if(!report.isFinished()) {
            MessageboxHelper.showMessage(this,
                    getResources().getString(R.string.confirmationNeeded),
                    getResources().getString(R.string.reportFinishMessage),
                    getResources().getString(R.string.yesTAG),
                    getResources().getString(R.string.noTag),
                    () -> reportFinish(true),
                    this::cancelReportFinish);
        } else {
            MessageboxHelper.showMessage(this,
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
        report.setFinished(finish);
        ReportRequestParametersResolver resolver = new ReportRequestParametersResolver(REST_REPORTUPLOAD_KEY, conf, false);
        resolver.postEntity(report, this);
    }

    @SuppressWarnings("deprecation")
    private void toggleMarkAdd(){
        logger.info("Toggling mark add state");
        Button btn = findViewById(R.id.btnAddMark);
        if(mode == 0) {
            btn.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
            mode = 1;
        }
        else{
            btn.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_add));
            mode = 0;
        }
        imageView.setTag(mode);
    }

    @SuppressWarnings("deprecation")
    private void notAddingMark(){
        logger.info("Exiting mark add state");
        Button btn = findViewById(R.id.btnAddMark);
        btn.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_add));
        mode = 0;
        imageView.setTag(mode);
    }

    private void save(Mark mark){
        logger.info("Started mark save request routine");

        mark.setDevice(DeviceHelper.getCurrentDevice());

        MarkRequestParametersResolver resolver = new MarkRequestParametersResolver(REST_MARKSAVE_KEY, conf, false);
        mark.getParent().setId(report.getPages().get(currentPage).getId());
        resolver.postEntity(mark, this);
    }

    private void remove(Mark mark){
        logger.info("Started mark remove request routine");

        MarkRequestParametersResolver resolver = new MarkRequestParametersResolver(REST_MARKREMOVE_KEY, conf, false);
        resolver.deleteEntity(mark, this);

        List<Mark> marks = report.getPages().get(currentPage).getMarks();
        marks.remove(mark);
    }

    private void close(boolean error){
        logger.info("Started close routine");
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

    private void nextPage(){
        if(currentPage < pageCount - 1) {
            logger.info("Started next page rendering routine");
            originalBitmap = WQCDocumentHelper.pageLoad(++currentPage, filePath, getResources());
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
            updatePointsDrawing();
        }
        updateButtonState();
    }

    private void previousPage(){
        if(currentPage > 0) {
            logger.info("Started previous page rendering routine");
            originalBitmap = WQCDocumentHelper.pageLoad(--currentPage, filePath, getResources());
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
            updatePointsDrawing();
        }
        updateButtonState();
    }

    private void updateButtonState(){
        logger.info("Started buttons state update routine");
        findViewById(R.id.btnNext).setEnabled(!(currentPage == pageCount - 1));
        findViewById(R.id.btnPrevious).setEnabled(!(currentPage == 0));
        findViewById(R.id.btnUndo).setEnabled(getLastPlacedUserMark() != null);
    }

    private void updateImageView(Bitmap bitmap){
        logger.info("Started image view update routine");
        imageView.setImageBitmap(bitmap);
    }

    private void addMark(float[] touchPoint){
        Spinner cmbRoles = findViewById(R.id.cmbRole);
        String roleToShow = cmbRoles.getSelectedItem().toString();
        logger.info("Started on-touch mark add routine");
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        Mark mark = new Mark();
        mark.setX(touchPoint[0]);
        mark.setY(touchPoint[1]);
        mark.setDevice(DeviceHelper.getCurrentDevice());
        mark.setAddedOn(SDF.format(Calendar.getInstance().getTime()));
        mark.setRoleToShow(roleToShow);

        markList.add(mark);
        notAddingMark();
        updateButtonState();
        save(mark);
    }

    private void undo(){
        Mark mark;
        mark = getLastPlacedUserMark();
        if(mark != null) {
            logger.info("Started mark undo routine");
            updateButtonState();
            notAddingMark();
            remove(mark);
        }
    }

    private Mark getLastPlacedUserMark(){
        logger.info("Started get last context mark routine");
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        Mark mark;
        if(markList != null && markList.size() > 0){
            for(int i = markList.size() - 1; i >= 0; i--) {
                mark = markList.get(i);
                if (mark.getDevice().getDeviceid().equals(DeviceHelper.getCurrentDevice().getDeviceid())) {
                    logger.info("Found a mark added by the current user");
                    return mark;
                }
            }
        }
        logger.info("No marks added by the current user were found");
        return null;
    }

    private void updatePointsDrawing(){
        logger.info("Started update points drawing routine");
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        if(markList != null) {
            logger.info("Marks found and are going to be drawn within the rendered page");
            currentBitmap = WQCDocumentHelper.updatePointsDrawing(markList, originalBitmap, getResources());
            updateImageView(currentBitmap);
        }
        else{
            logger.info("No marks were found. Only the original document page will be rendered");
            updateImageView(originalBitmap);
        }
    }

    @Override
    public void onTouch(float[] touchPoint) {
        Mark m = touchOnExistingMark(touchPoint);
        if(m == null) {
            if(mode == 1) {
                addMark(touchPoint);
            }
        } else {
            notAddingMark();
            lastTouchedMark = m;
            displayMarkInfo(m);
        }
    }

    public void displayMarkInfo(Mark m){
        boolean canRemove = false;
        final String markRole = m.getRoleToShow();

        String message = String.format("%s: %s \n%s: %s \n%s: %s",
                getResources().getString(R.string.usernameTag),
                m.getDevice().getName(),
                getResources().getString(R.string.roleTag),
                markRole,
                getResources().getString(R.string.addeddateTag),
                m.getAddedOn());
        if (!DeviceHelper.isOnlyRole("TE") &&
                m.getDevice().getDeviceid().equals(DeviceHelper.getCurrentDevice().getDeviceid())
                && !report.isFinished()) {
            canRemove = true;
        }
        if (canRemove){
            MessageboxHelper.showMessage(this, "", message,
                    getResources().getString(R.string.removeTag),
                    getResources().getString(R.string.cancelTag),
                    () -> remove(lastTouchedMark), null);
        } else {
            MessageboxHelper.showMessage(this, message,
                    getResources().getString(R.string.okTag),
                    () -> remove(lastTouchedMark));
        }

    }

    public Mark touchOnExistingMark(float[] touchPoint){
        List<Mark> marks = report.getPages().get(currentPage).getMarks();
        for(int i = 0; i < marks.size(); i++){
            Mark m = marks.get(i);
            int radius = WQCDocumentHelper.radius;
            float mX, mY, pX, pY;
            mX = m.getX() * currentBitmap.getWidth();
            mY = m.getY() * currentBitmap.getHeight();
            pX = touchPoint[0] * currentBitmap.getWidth();
            pY = touchPoint[1] * currentBitmap.getHeight();
            if(pX >= (mX - radius) && pX <= (mX + radius)
                    && pY >= (mY - radius) && pY <= (mY + radius)){
                return m;
            }
        }
        return null;
    }

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        logger.info("Started server http response handler");
        toggleControls(true);
        if(result instanceof SuccessResult) {
            switch (result.getRequestCode()) {
                case REST_MARKSAVE_KEY:
                    String res = ResultService.getLocationResult(result);
                    logger.info("The response was got from a mark save request, started mark id update commands");
                    List<Mark> marks = report.getPages().get(currentPage).getMarks();
                    Long id = Long.valueOf(res.substring(res.lastIndexOf("/") + 1));
                    marks.stream().filter(m -> m.getId() == 0L).findFirst().ifPresent(m -> m.setId(id));
                    /*Toast.makeText(this, R.string.changesSavedMessage, Toast.LENGTH_SHORT).show();*/
                    break;
                case REST_MARKREMOVE_KEY:
                    /*
                    writeData("The response was got from a mark remove request");
                    Toast.makeText(this, R.string.changesSavedMessage, Toast.LENGTH_SHORT).show();
                    */
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    logger.info("The response was got from a qr project load request, trying to parse result to project entity");
                    Project project = ResultService.getSingleResult(result, Project.class);

                    logger.info("Parse successful, getting report from project");
                    report = (CheckReport) project.getDrawingRefs().get(0).getReports().stream()
                            .filter(r -> r.getId().equals(report.getId()))
                            .findFirst()
                            .orElse(null);
                    if (report == null) {
                        ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.INVALID_ENTITY, getResources().getString(R.string.unknownErrorMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
                        ErrorResponseHandler.handle(err, this, () -> close(true));
                    } else {
                        updatePointsDrawing();
                        toggleControls(true);
                    }
                    break;
            }
            updatePointsDrawing();
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, this::projectLoad);
        }
    }

    @Override
    public void toggleControls(boolean bResume){
        runOnUiThread(() ->
                {
                    logger.info("Started controls toggle routine");
                    (findViewById(R.id.btnAddMark)).setEnabled(bResume && !DeviceHelper.isOnlyRole("TE"));

                    if (bResume) {
                        (findViewById(R.id.btnUndo)).setEnabled(getLastPlacedUserMark() != null);
                    } else {
                        (findViewById(R.id.btnUndo)).setEnabled(false);
                    }

                    (findViewById(R.id.btnNext)).setEnabled(currentPage < (report.getPagesCount() - 1) && bResume);
                    (findViewById(R.id.btnPrevious)).setEnabled(currentPage > 0 && bResume);
                    (findViewById(R.id.btnExit)).setEnabled(bResume);
                    if (!bResume) {
                        (findViewById(R.id.pbCheckReportEdit)).setVisibility(View.VISIBLE);
                    } else {
                        (findViewById(R.id.pbCheckReportEdit)).setVisibility(View.INVISIBLE);
                    }
                    (findViewById(R.id.chkFinished)).setEnabled(bResume && !DeviceHelper.isOnlyRole("TE"));
                    (findViewById(R.id.cmbRole)).setEnabled(bResume && !DeviceHelper.isOnlyRole("TE"));

                    if(DeviceHelper.isOnlyRole("TE") || report.isFinished()){
                        findViewById(R.id.btnAddMark).setVisibility(View.INVISIBLE);
                        findViewById(R.id.btnUndo).setVisibility(View.INVISIBLE);
                        (findViewById(R.id.cmbRole)).setVisibility(View.INVISIBLE);
                    } else {
                        findViewById(R.id.btnAddMark).setVisibility(View.VISIBLE);
                        findViewById(R.id.btnUndo).setVisibility(View.VISIBLE);
                        (findViewById(R.id.cmbRole)).setVisibility(View.VISIBLE);
                    }

                    CheckBox chkFinished = findViewById(R.id.chkFinished);
                    chkFinished.setChecked(report.isFinished());

                }
        );
    }

    @Override
    public void onFatalError() {

    }

    private void projectLoad(){
        logger.info("Started project load request routine");
        ProjectRequestParametersResolver resolver = new ProjectRequestParametersResolver(REST_QRPROJECTLOAD_KEY, conf, true);
        resolver.getEntity(ProjectHelper.getProject(), this);
    }

    @Override
    public void messageReceived(Map<String, String> data) {
        String qrCode = data.get("qrCode");
        if(qrCode != null) {
//            qrCode = data.replace("\\", "");
            if (ProjectHelper.getQrCode().equals(qrCode)) {
                logger.info("Project changed by another user/device. Data reload triggered");
                runOnUiThread(this::projectLoad);
            }
        }
    }
}
