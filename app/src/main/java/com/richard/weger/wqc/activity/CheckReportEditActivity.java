package com.richard.weger.wqc.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.firebird.FirebaseHelper;
import com.richard.weger.wqc.helper.ActivityHelper;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.PdfHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.helper.WQCDocumentHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.service.MarkRequestParametersResolver;
import com.richard.weger.wqc.service.ReportRequestParametersResolver;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.LoggerManager;
import com.richard.weger.wqc.util.TouchImageView;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_ID_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_CONFIGLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_MARKREMOVE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_MARKSAVE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_REPORTUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.SDF;

public class CheckReportEditActivity extends Activity implements TouchImageView.ChangeListener,
        RestTemplateHelper.RestResponseHandler,
        FirebaseHelper.FirebaseListener, TouchImageView.SwipeHandler {

    CheckReport report;
    Long reportId;
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

    Logger logger;

    @Override
    public void onBackPressed(){

    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        ConfigurationsManager.loadServerConfig(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = LoggerManager.getLogger(getClass());

        Intent intent = getIntent();
        reportId = intent.getLongExtra(REPORT_ID_KEY, -1);
        if(reportId < 0){
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void configImageView(){
        logger.info("Started image view config");
        imageView = findViewById(R.id.ivDocument);
        imageView.setMaxZoom(12f);
        imageView.setChangeListener(this);
        imageView.setSwipeHandler(this);
        imageView.setTag(mode);

        pageCount = PdfHelper.getPageCount(filePath);
        if(pageCount == 0 || pageCount != report.getPages().size()){
            setResult(RESULT_CANCELED);
            finish();
        }

        originalBitmap = WQCDocumentHelper.pageLoad(currentPage, filePath, getResources());

        if(originalBitmap != null) {
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
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

        ImageButton iBtn = findViewById(R.id.btnAddMark);
        iBtn.setOnClickListener(view -> toggleMarkAdd());

        iBtn = findViewById(R.id.btnUndo);
        iBtn.setOnClickListener(view -> undo());

        iBtn = findViewById(R.id.btnExit);
        iBtn.setOnClickListener(view -> close(false));

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
        report.setFinished(finish);
        ReportRequestParametersResolver resolver = new ReportRequestParametersResolver(REST_REPORTUPLOAD_KEY, conf, false);
        resolver.postEntity(report, this);
    }

    @SuppressWarnings("deprecation")
    private void toggleMarkAdd(){
        logger.info("Toggling mark add state");
        ImageButton btn = findViewById(R.id.btnAddMark);
        if(mode == 0) {
            btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel));
            toggleControls(false);
            findViewById(R.id.pbCheckReportEdit).setVisibility(View.INVISIBLE);
            btn.setEnabled(true);
            mode = 1;
        }
        else{
            btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));
            mode = 0;
            toggleControls(true);
            btn.setEnabled(true);
        }
        imageView.setTag(mode);
    }

    private void notAddingMark(){
        mode = 1;
        toggleMarkAdd();
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
        finishAndRemoveTask();
        stopLockTask();
        super.onDestroy();
    }

    private void changePage(int dWork){
        int dir;
        float lastX;

        dir = dWork / Math.abs(dWork);
        lastX = 24;

        if(((TouchImageView)findViewById(R.id.ivDocument)).isZoomed()){
            return;
        }

        if((dir > 0 && currentPage < (pageCount - 1)) || (dir < 0 && currentPage > 0)) {

            ImageView iv = findViewById(R.id.ivDocument);
            ImageView ivWork = findViewById(R.id.ivPrevNext);

            iv.setClickable(false);
            iv.setEnabled(false);
            ivWork.setClickable(false);
            ivWork.setEnabled(false);

            ivWork.setImageBitmap(currentBitmap);
            ivWork.setVisibility(View.VISIBLE);

            iv.setX(dir * getWindow().getDecorView().getWidth());

            currentPage += dir;
            originalBitmap = WQCDocumentHelper.pageLoad(currentPage, filePath, getResources());
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
            updatePointsDrawing();

            ObjectAnimator ivAnim = ObjectAnimator.ofFloat(iv, View.TRANSLATION_X, lastX).setDuration(150);
            ObjectAnimator ivwAnim = ObjectAnimator.ofFloat(ivWork, View.TRANSLATION_X, -dir * getWindow().getDecorView().getWidth()).setDuration(150);

            AnimatorSet animations = new AnimatorSet();
            animations.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ivWork.setVisibility(View.INVISIBLE);
                    ivWork.animate().translationX(lastX).setDuration(0);
                    iv.setClickable(true);
                    iv.setEnabled(true);
                    ivWork.setClickable(true);
                    ivWork.setEnabled(true);
                }
            });
            animations.play(ivAnim).with(ivwAnim);
            animations.start();
        } else {
            ImageView iv = findViewById(R.id.ivDocument);

            iv.setClickable(false);
            iv.setEnabled(false);

            ObjectAnimator ivAnim = ObjectAnimator.ofFloat(iv, View.TRANSLATION_X, -dir * (float)(getWindow().getDecorView().getWidth() / 3)).setDuration(100);

            AnimatorSet animations = new AnimatorSet();
            animations.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    iv.animate().translationX(lastX).setDuration(100).withEndAction(() -> {
                        iv.setClickable(true);
                        iv.setEnabled(true);
                    });
                }
            });
            animations.play(ivAnim);
            animations.start();
        }
    }

//    private void updateButtonState(){
//        logger.info("Started buttons state update routine");
////        findViewById(R.id.btnNext).setEnabled(!(currentPage == pageCount - 1));
////        findViewById(R.id.btnPrevious).setEnabled(!(currentPage == 0));
//        findViewById(R.id.btnUndo).setEnabled(getLastPlacedUserMark() != null);
//    }

    private void updateImageView(Bitmap bitmap){
        logger.info("Started image view update routine");
        imageView.setImageBitmap(bitmap);
        TextView tv = (findViewById(R.id.tvCurrentPage));
        tv.setText(String.format(App.getContext().getResources().getConfiguration().getLocales().get(0), "%d / %d", currentPage + 1, pageCount));
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
        save(mark);
    }

    private void undo(){
        Mark mark;
        mark = getLastPlacedUserMark();
        if(mark != null) {
            logger.info("Started mark undo routine");
            toggleControls(false);
//            updateButtonState();
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
                toggleControls(false);
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
            AlertHelper.showMessage(this, "", message,
                    getResources().getString(R.string.removeTag),
                    getResources().getString(R.string.cancelTag),
                    () -> remove(lastTouchedMark), null);
        } else {
            AlertHelper.showMessage(this, message,
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

    private void setFilePath(Project project){
        String folder;
        folder = StringHelper.getPdfsFolderPath(project);

        filePath = folder.concat(File.separator).concat(report.getFileName());

        if(!FileHelper.isValidFile(filePath)){
            ActivityHelper.setWaitingLayout(this);
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_FILE_ACCESS_EXCEPTION, getResources().getString(R.string.fileNotFoundMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
            ErrorResponseHandler.handle(err, this, () ->{
                setResult(RESULT_OK);
                finish();
            });
        }
    }

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        logger.info("Started server http response handler");
        if(result instanceof SuccessResult) {
            switch (result.getRequestCode()) {
                case REST_MARKSAVE_KEY:
                case REST_MARKREMOVE_KEY:
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    logger.info("Got project from received request");
                    Project project = ResultService.getSingleResult(result, Project.class);

                    report = (CheckReport) project.getDrawingRefs().get(0).getReports().stream()
                            .filter(r -> r.getId().equals(reportId))
                            .findFirst()
                            .orElse(null);
                    if (report == null) {
                        ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.INVALID_ENTITY, getResources().getString(R.string.unknownErrorMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
                        ErrorResponseHandler.handle(err, this, () -> close(true));
                        return;
                    }
                    setFilePath(project);
                    inflateActivityLayout();
                    notAddingMark();
                    break;
                case REST_CONFIGLOAD_KEY:
                    ParamConfigurations c = ResultService.getSingleResult(result, ParamConfigurations.class);
                    ConfigurationsManager.setServerConfig(c);
                    conf = c;
                    FirebaseHelper.firebaseConfig(this);
                    break;
            }
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, () -> ProjectHelper.projectLoad(this));
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

    @Override
    public void messageReceived(String qrCode, Long id) {
        logger.info("Project was updated by another user. Triggering reload from CheckReport activity");
        runOnUiThread(() -> ProjectHelper.projectLoad(this, false));
    }

    @Override
    public void onConnectionSuccess() {
        ProjectHelper.projectLoad(this);
    }

    @Override
    public void onSwipeRight() {
        changePage(-1);
    }

    @Override
    public void onSwipeLeft() {
        changePage(1);
    }

    @Override
    public void onSwipeTop() {

    }

    @Override
    public void onSwipeBottom() {

    }
}
