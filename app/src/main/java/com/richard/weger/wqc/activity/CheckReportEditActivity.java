package com.richard.weger.wqc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.CheckReportEditAdapter;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.fragment.CheckReportEditFragment;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.DeviceHelper;
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
import com.richard.weger.wqc.service.MarkRequestParametersResolver;
import com.richard.weger.wqc.service.ReportRequestParametersResolver;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.LoggerManager;
import com.richard.weger.wqc.views.TouchImageView;

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
import static com.richard.weger.wqc.util.App.getStringResource;

public class CheckReportEditActivity extends FragmentActivity implements TouchImageView.ImageTouchListener,
        RestTemplateHelper.RestResponseHandler,
        IMessagingListener, CheckReportEditFragment.MarkTouchListener{

    CheckReport report;
    Long reportId;
    ParamConfigurations conf;
    boolean onCreateChain = false;

    ViewPager mPager;
    CheckReportEditAdapter adapter;
    boolean isWaiting = false;

    private int mode = 0;
    // mode 0 -> zoom / pan;
    // mode 1 -> addMark;

    Logger logger;

    @Override
    public void onBackPressed(){}

    @Override
    protected void onResume(){
        super.onResume();
        ConfigurationsManager.loadServerConfig(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onCreateChain = true;

        logger = LoggerManager.getLogger(CheckReportEditActivity.class);

        Intent intent = getIntent();
        reportId = intent.getLongExtra(REPORT_ID_KEY, -1);
        if(reportId < 0){
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @SuppressWarnings("unchecked")
    private void inflateActivityLayout(Project project){
        if(onCreateChain) {
            logger.info("Setting content view");
            setContentView(R.layout.activity_check_report_edit);
        }

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

        if(onCreateChain) {
            adapter = new CheckReportEditAdapter(getSupportFragmentManager(),
                    report.getFileName(),
                    StringHelper.getPdfsFolderPath(project),
                    report.getPages(), this, this);
            mPager = findViewById(R.id.pager);
            mPager.setAdapter(adapter);
            mPager.setCurrentItem(0);
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int state) {}
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                public void onPageSelected(int position) {
                    onPageChanged();
                }
            });
            onCreateChain = false;
        } else {
            adapter.setPages(report.getPages());
            adapter.notifyDataSetChanged();
        }
        onPageChanged();
        setListeners();
    }

    private void onPageChanged(){
        adapter.setCurrentPosition(mPager.getCurrentItem());
        TextView tvCurrPage = findViewById(R.id.tvCurrentPage);
        tvCurrPage.setText(String.format(App.getLocale(), "%d/%d", mPager.getCurrentItem() + 1, adapter.getCount()));
        toggleControls(true);
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
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_UNFINISHED_CHECKREPORT_WARNING, getStringResource(R.string.noMarksMessage), ErrorResult.ErrorLevel.WARNING);
                ErrorResponseHandler.handle(err, this::cancelReportFinish);
            }
        });

    }

    private void shouldChangeReportState(){
        if(!report.isFinished()) {
            AlertHelper.showMessage(
                    getStringResource(R.string.confirmationNeeded),
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
        report.setFinished(finish);
        ReportRequestParametersResolver resolver = new ReportRequestParametersResolver(REST_REPORTUPLOAD_KEY, conf, false);
        resolver.postEntity(report, this);
    }

    @SuppressWarnings("deprecation")
    private void toggleMarkAdd(){
        logger.info("Toggling mark add state");
        ImageButton btn = findViewById(R.id.btnAddMark);
        if(mode == 0) {
            btn.setImageDrawable(App.getDrawableResource(R.drawable.ic_cancel));
            toggleControls(false);
            findViewById(R.id.pbCheckReportEdit).setVisibility(View.INVISIBLE);
            btn.setEnabled(true);
            mode = 1;
        } else {
            btn.setImageDrawable(App.getDrawableResource(R.drawable.ic_add));
            mode = 0;
            toggleControls(true);
            btn.setEnabled(true);
        }
    }

    private void notAddingMark(){
        mode = 1;
        toggleMarkAdd();
    }

    private void save(Mark mark){
        logger.info("Started mark save request routine");

        mark.setDevice(DeviceHelper.getCurrentDevice());

        Page parent = mark.getParent();
        Long parentId = report.getPages().get(adapter.getCurrentPosition()).getId();
        parent.setId(parentId);

        MarkRequestParametersResolver resolver = new MarkRequestParametersResolver(REST_MARKSAVE_KEY, conf, false);
        resolver.postEntity(mark, this);
    }

    private void remove(Mark mark){
        logger.info("Started mark remove request routine");

        MarkRequestParametersResolver resolver = new MarkRequestParametersResolver(REST_MARKREMOVE_KEY, conf, false);
        resolver.deleteEntity(mark, this);

    }

    private void close(boolean error){
        MessagingHelper.getServiceInstance().removeListener();
        logger.info("Started close routine");
        if(error) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finishAndRemoveTask();
    }

    private void addMark(float[] touchPoint){
        Spinner cmbRoles = findViewById(R.id.cmbRole);
        String roleToShow = cmbRoles.getSelectedItem().toString();
        logger.info("Started on-touch mark add routine");
        List<Mark> markList = report.getPages().get(adapter.getCurrentPosition()).getMarks();
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
        if(!isWaiting) {
            toggleControls(false);
            Mark mark;
            mark = getLastPlacedUserMark();
            if (mark != null) {
                isWaiting = true;
                logger.info("Started mark undo routine");
//            updateButtonState();
                notAddingMark();
                remove(mark);
            } else {
                toggleControls(true);
            }
        }
    }

    private Mark getLastPlacedUserMark(){
        logger.info("Started get last context mark routine");
        List<Mark> markList = report.getPages().get(adapter.getCurrentPosition()).getMarks();
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

    @Override
    public void onTouch(float[] touchPoint) {
        if(mode == 1) {
            toggleControls(false);
            addMark(touchPoint);
        }
    }

    @Override
    public void onMarkTouch(Mark m) {
        boolean canRemove = false;
        final String markRole = m.getRoleToShow();

        notAddingMark();

        String message = String.format("%s: %s \n%s: %s \n%s: %s",
                getStringResource(R.string.usernameTag),
                m.getDevice().getName(),
                getStringResource(R.string.roleTag),
                markRole,
                getStringResource(R.string.addeddateTag),
                m.getAddedOn());
        if (!DeviceHelper.isOnlyRole("TE") &&
                m.getDevice().getDeviceid().equals(DeviceHelper.getCurrentDevice().getDeviceid())
                && !report.isFinished()) {
            canRemove = true;
        }
        if (canRemove){
            AlertHelper.showMessage("", message,
                    getStringResource(R.string.removeTag),
                    getStringResource(R.string.cancelTag),
                    () -> remove(m), null, this);
        } else {
            AlertHelper.showMessage(message,
                    getStringResource(R.string.okTag),
                    null);
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
                        ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.INVALID_ENTITY, getStringResource(R.string.unknownErrorMessage), ErrorResult.ErrorLevel.SEVERE);
                        ErrorResponseHandler.handle(err, () -> close(true));
                        return;
                    }
                    inflateActivityLayout(project);
                    notAddingMark();
                    isWaiting = false;
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
            ErrorResponseHandler.handle(err, () -> ProjectHelper.projectLoad(this));
        }
    }

    @Override
    public void toggleControls(boolean bResume){
        logger.info("Started controls toggle routine");
        (findViewById(R.id.btnAddMark)).setEnabled(bResume && !DeviceHelper.isOnlyRole("TE"));

        if (bResume) {
            (findViewById(R.id.btnUndo)).setEnabled(getLastPlacedUserMark() != null);
        } else {
            (findViewById(R.id.btnUndo)).setEnabled(false);
        }

        (findViewById(R.id.btnExit)).setEnabled(true);
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

    @Override
    public void onFatalError() {

    }

    @Override
    public boolean shouldNotifyChange(String qrCode, Long id, Long parentId) {
        if(ProjectHelper.shouldRefresh(report, id, parentId)) {
            logger.info("The current report was updated by another user. Triggering reload from ItemReport activity");
            try{
                runOnUiThread(()-> toggleControls(false));
            } catch (Exception e){
                logger.warning("Unable to disable controls for project refresh routine!");
            }
            ProjectHelper.projectLoad(this, false);
            return true;
        }
        logger.info("Another report was updated by another user. There is no need to refresh the contents.");
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

}
