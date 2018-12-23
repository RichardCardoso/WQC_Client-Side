package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.firebird.FirebirdMessagingService;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.helper.WQCDocumentHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.DeviceManager;
import com.richard.weger.wqc.helper.JsonHelper;
import com.richard.weger.wqc.helper.PdfHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.util.TouchImageView;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.richard.weger.wqc.constants.AppConstants.*;
import static com.richard.weger.wqc.helper.LogHelper.writeData;

public class CheckReportEditActivity extends Activity implements TouchImageView.ChangeListener,
        RestTemplateHelper.RestHelperResponse, FirebirdMessagingService.FirebaseListener {

    CheckReport report;
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
    public void onBackPressed(){

    }

    @Override
    protected void onPause(){
        super.onPause();
        close(false);
        handler.removeCallbacks(runnable);
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

        String folder;

        writeData("Getting intent data");
        Intent intent = getIntent();
        int id;
        id = intent.getIntExtra(REPORT_ID_KEY, -1);
        if(id < 0){
            writeData("Invalid id found at the recovered data. Aborting");
            setResult(RESULT_CANCELED);
            finish();
        }
        writeData("Getting project from intent data");
        Project project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        writeData("Linking project references");
        ProjectHelper.linkReferences(project);
        writeData("Getting report from project");
        report = (CheckReport) project.getDrawingRefs().get(0).getReports().get(id);

        writeData("Generating original documents folder name");
        folder = StringHelper.getProjectFolderPath(project).concat("Originals/");

        filePath = folder.concat(File.separator).concat(report.getFileName());

        writeData("Starting activity start from check report edit screen");
        inflateActivityLayout();
    }

    private void configImageView(int id){
        writeData("Started configuring image view");
        imageView = findViewById(id);
        imageView.setMaxZoom(12f);
        imageView.setChangeListener(this);
        imageView.setTag(mode);

        writeData("Getting document pages count");
        pageCount = PdfHelper.getPageCount(filePath);
        if(pageCount == 0 || pageCount != report.getPages().size()){
            // Toast.makeText(this, R.string.dataRecoverError, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        writeData("Getting current page bitmap");
        originalBitmap = WQCDocumentHelper.pageLoad(currentPage, filePath, getResources());

        writeData("Updating image view with current page bitmap");
        if(originalBitmap != null) {
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
            init();
            setListeners();
            updatePointsDrawing();
        } else {
            writeData("Image view update error");
            Toast.makeText(this, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
//            dataLoadError();
        }
    }

    private void inflateActivityLayout(){
        writeData("Setting content view");
        setContentView(R.layout.activity_check_report_edit);

        if(DeviceManager.getCurrentDevice().getRole().equals("TE")){
            findViewById(R.id.btnAddMark).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnUndo).setVisibility(View.INVISIBLE);
        }

        configImageView(R.id.ivDocument);
    }

    private void init(){
        writeData("Updating buttons state");
        updateButtonState();
    }

    private void setListeners(){
        writeData("Setting buttons listeners");
        Button btn = findViewById(R.id.btnNext);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });

        btn = findViewById(R.id.btnPrevious);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousPage();
            }
        });

        btn = findViewById(R.id.btnAddMark);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMarkAdd();
            }
        });

        btn = findViewById(R.id.btnUndo);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undo();
            }
        });

        btn = findViewById(R.id.btnExit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close(false);
            }
        });
    }

    private void toggleMarkAdd(){
        writeData("Toggling mark add state");
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

    private void notAddingMark(){
        writeData("Exiting mark add state");
        Button btn = findViewById(R.id.btnAddMark);
        btn.setBackground(getResources().getDrawable(android.R.drawable.ic_menu_add));
        mode = 0;
        imageView.setTag(mode);
    }

    private void save(Mark mark){
        writeData("Started mark save request routine");

        toggleControls(false);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);

        mark.setDevice(DeviceManager.getCurrentDevice());

        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_MARKSAVE_KEY);
        uriBuilder.setMark(mark);
        uriBuilder.setProject(report.getDrawingref().getProject());
        uriBuilder.setPage(report.getPages().get(currentPage));
        restTemplateHelper.execute(uriBuilder);
        writeData("Finished mark save request routine");
    }

    private void remove(Mark mark){
        writeData("Started mark remove request routine");
        toggleControls(false);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_MARKREMOVE_KEY);
        uriBuilder.setMark(mark);
        uriBuilder.setProject(report.getDrawingref().getProject());
        uriBuilder.setPage(report.getPages().get(currentPage));
        restTemplateHelper.execute(uriBuilder);
        List<Mark> marks = report.getPages().get(currentPage).getMarks();
        marks.remove(mark);
        writeData("Finished mark remove request routine");
    }

    private void close(boolean error){
        writeData("Started close routine");
        if(error) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
        }
        finish();
        finishAndRemoveTask();
        stopLockTask();
        writeData("Check report activity closed");
        super.onDestroy();
    }

    private void nextPage(){
        if(currentPage < pageCount - 1) {
            writeData("Started next page rendering routine");
            originalBitmap = WQCDocumentHelper.pageLoad(++currentPage, filePath, getResources());
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
            updatePointsDrawing();
            writeData("Finished next page rendering routine");
        }
        updateButtonState();
    }

    private void previousPage(){
        if(currentPage > 0) {
            writeData("Started previous page rendering routine");
            originalBitmap = WQCDocumentHelper.pageLoad(--currentPage, filePath, getResources());
            currentBitmap = WQCDocumentHelper.bitmapCopy(originalBitmap);
            updatePointsDrawing();
            writeData("Finished previous page rendering routine");
        }
        updateButtonState();
    }

    private void updateButtonState(){
        writeData("Started buttons state update routine");
        findViewById(R.id.btnNext).setEnabled(!(currentPage == pageCount - 1));
        findViewById(R.id.btnPrevious).setEnabled(!(currentPage == 0));
        findViewById(R.id.btnUndo).setEnabled(canUndo());
        writeData("Finished buttons state update routine");
    }

    private void updateImageView(Bitmap bitmap){
        writeData("Started image view update routine");
        imageView.setImageBitmap(bitmap);
        writeData("Finished image view update routine");
    }

    private void addMark(float[] touchPoint){
        writeData("Started on-touch mark add routine");
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        Mark mark = new Mark();
        mark.setX(touchPoint[0]);
        mark.setY(touchPoint[1]);
        mark.setDevice(DeviceManager.getCurrentDevice());

        mark.setAddedOn(SDF.format(Calendar.getInstance().getTime()));

        markList.add(mark);
        notAddingMark();
        updateButtonState();
        save(mark);
        writeData("Finished on-touch mark add routine");
    }

    private void undo(){
        Mark mark;
        mark = getLastContextMark();
        if(mark != null) {
            writeData("Started mark undo routine");
            updateButtonState();
            notAddingMark();
            remove(mark);
            writeData("Finished mark undo routine");
        }
    }

    private Mark getLastContextMark(){
        writeData("Started get last context mark routine");
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        Mark mark;
        if(markList != null && markList.size() > 0){
            for(int i = markList.size() - 1; i >= 0; i--) {
                mark = markList.get(i);
                if (mark.getDevice().getRole().equals(DeviceManager.getCurrentDevice().getRole())) {
                    writeData("Found a mark added by the current user");
                    return mark;
                }
            }
        }
        writeData("No marks added by the current user were found");
        return null;
    }

    private void updatePointsDrawing(){
        writeData("Started update points drawing routine");
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        if(markList != null) {
            writeData("Marks found and are going to be drawn within the rendered page");
            currentBitmap = WQCDocumentHelper.updatePointsDrawing(markList, originalBitmap, getResources());
            updateImageView(currentBitmap);
        }
        else{
            writeData("No marks were found. Only the original document page will be rendered");
            updateImageView(originalBitmap);
        }
    }

    private boolean canUndo(){
        writeData("Started can undo routine");
        List<Mark> marks = report.getPages().get(currentPage).getMarks();
        for(int i = 0; i < marks.size(); i++){
            if(marks.get(i).getDevice().getRole().equals(DeviceManager.getCurrentDevice().getRole())){
                writeData("There are marks added by the current user so he is able to remove them");
                return true;
            }
        }
        writeData("No marks added by the current user were found so he cant remove any existing mark");
        return false;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String positiveButtonTag, negativeButtonTag;
        boolean canRemove = false;
        final String markRole = m.getDevice().getRole();

        builder.setMessage(String.format("%s: %s \n%s: %s \n%s: %s",
                getResources().getString(R.string.usernameTag),
                m.getDevice().getName(),
                getResources().getString(R.string.roleTag),
                m.getDevice().getRole(),
                getResources().getString(R.string.addeddateTag),
                m.getAddedOn()));
        if (!DeviceManager.getCurrentDevice().getRole().equals("TE") &&
                m.getDevice().getRole().equals(DeviceManager.getCurrentDevice().getRole())) {
            canRemove = true;
        }
        if (canRemove){
            positiveButtonTag = getResources().getString(R.string.removeTag);
            negativeButtonTag = getResources().getString(R.string.cancelTag);

            builder.setNegativeButton(negativeButtonTag, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton(positiveButtonTag, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (markRole.equals(DeviceManager.getCurrentDevice().getRole())){
                        remove(lastTouchedMark);
                    }
                }
            });
        } else {
            positiveButtonTag = getResources().getString(R.string.okTag);
            builder.setPositiveButton(positiveButtonTag, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }

        builder.show();
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

    private void toggleControls(boolean bResume){
        writeData("Started controls toggle routine");
        (findViewById(R.id.btnAddMark)).setEnabled(bResume);

        if(bResume) {
            (findViewById(R.id.btnUndo)).setEnabled(canUndo());
        } else {
            (findViewById(R.id.btnUndo)).setEnabled(false);
        }

        (findViewById(R.id.btnNext)).setEnabled(currentPage < report.getPagesCount());
        (findViewById(R.id.btnPrevious)).setEnabled(currentPage > 0);
        (findViewById(R.id.btnExit)).setEnabled(bResume);
        if(!bResume) {
            (findViewById(R.id.pbCheckReportEdit)).setVisibility(View.VISIBLE);
        } else {
            (findViewById(R.id.pbCheckReportEdit)).setVisibility(View.INVISIBLE);
        }
        writeData("Finished controls toggle routine");
    }

    private void dataLoadError(){
        writeData("Showing default error message");
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getResources().getString(R.string.dataRecoverError);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                close(true);
            }
        });
        builder.show();
    }

    private void dataLoadError(String customMessage){
        writeData("Showing custom error message");
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
        writeData("Started project load request routine");
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_QRPROJECTLOAD_KEY);
        uriBuilder.getParameters().add(StringHelper.getQrText(report.getDrawingref().getProject()));
        restTemplateHelper.execute(uriBuilder);
        toggleControls(false);
        writeData("Finished project load request routine");
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        writeData("Started server http response handler");
        toggleControls(true);
        if(result != null) {
            writeData("A not null response was received");
            if (!result.equals(App.getContext().getResources().getString(R.string.drawingLockedMessage))) {
                writeData("The current drawing is not locked");
                if (requestCode.equals(REST_MARKSAVE_KEY)) {
                    writeData("The response was got from a mark save request");
                    writeData("Started mark id update commands");
                    List<Mark> marks = report.getPages().get(currentPage).getMarks();
                    marks.get(marks.size() - 1).setId(JsonHelper.toObject(result, Mark.class).getId());
                    writeData("Finished mark id update commands");
                } else if (requestCode.equals(REST_MARKREMOVE_KEY)) {
                    writeData("The response was got from a mark remove request");
//                    writeData("Started mark remove commands processing");
//                    List<Mark> markList = report.getPages().get(currentPage).getMarks();
//                    markList.remove(getLastContextMark());
//                    writeData("Finished mark remove commands processing");
                } else if (requestCode.equals(REST_QRPROJECTLOAD_KEY)) {
                    writeData("The response was got from a qr project load request");
                    writeData("Trying to parse result to project entity");
                    Project project = ProjectHelper.fromJson(result);
                    writeData("Parse successful");
                    writeData("Getting report from project");

                    report = (CheckReport) project.getDrawingRefs().get(0).getReport(report.getId());
                    if(report == null){
                        dataLoadError();
                    }
                    writeData("Report got successful");
                    updatePointsDrawing();
                    toggleControls(true);
                }
            } else {
                writeData("The current drawing is locked by another user. Write attempt aborted");
                toggleControls(false);
                projectLoad();
                dataLoadError(App.getContext().getResources().getString(R.string.drawingLockedMessage));
            }
            updatePointsDrawing();
        } else {
            writeData("A generic error occurred while trying to get a response from the server. The requestCode was:" + requestCode);
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
