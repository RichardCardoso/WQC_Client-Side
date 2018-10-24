package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.DeviceManager;
import com.richard.weger.wqc.util.JsonHandler;
import com.richard.weger.wqc.util.PdfHandler;
import com.richard.weger.wqc.util.ProjectHandler;
import com.richard.weger.wqc.util.StringHandler;
import com.richard.weger.wqc.util.TouchImageView;
import com.richard.weger.wqc.util.WQCDocumentHandler;

import java.io.File;
import java.util.List;

import static com.richard.weger.wqc.util.AppConstants.*;

public class CheckReportEditActivity extends Activity implements TouchImageView.ChangeListener,
        RestTemplateHelper.HttpHelperResponse{

    CheckReport report;
    Bitmap originalBitmap = null,
            currentBitmap = null;
    int currentPage = 0,
        pageCount = 0;
    TouchImageView imageView = null;
    String filePath;
    int mode = 0;
    // mode 0 = zoom / pan
    // mode 1 = add mark

    @Override
    public void onBackPressed(){

    }

    @Override
    protected void onPause(){
        super.onPause();
        exit(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String folder;

        Intent intent = getIntent();
        int id;
        id = intent.getIntExtra(REPORT_ID_KEY, -1);
        if(id <=0){
            setResult(RESULT_CANCELED);
            finish();
        }
        Project project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        ProjectHandler.linkReferences(project);
        report = (CheckReport) project.getDrawingRefs().get(0).getReports().get(id);

        folder = StringHandler
                .generateProjectFolderName(App.getContext().getExternalFilesDir(null), project)
                .concat("Originals/");

        filePath = folder.concat(File.separator).concat(report.getClientPdfPath());

        activityStart();
    }

    private void configImageView(int id){
        imageView = findViewById(id);
        imageView.setMaxZoom(12f);
        imageView.setChangeListener(this);
        imageView.setTag(mode);

        pageCount = PdfHandler.getPageCount(filePath);
        if(pageCount == 0){
            // Toast.makeText(this, R.string.dataRecoverError, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        originalBitmap = WQCDocumentHandler.pageLoad(currentPage, filePath, getResources());
        if(originalBitmap != null) {
            currentBitmap = WQCDocumentHandler.bitmapCopy(originalBitmap);
            init();
            setListeners();
            updatePointsDrawing();
        } else {
            Toast.makeText(this, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
//            dataLoadError();
        }
    }

    private void activityStart(){

        setContentView(R.layout.activity_check_report_edit);

        configImageView(R.id.ivDocument);

    }

    private void init(){
        updateButtonState();
    }

    private void setListeners(){
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
                exit(false);
            }
        });
    }

    private void toggleMarkAdd(){
        Button btn = findViewById(R.id.btnAddMark);
        if(btn.getText().equals(getResources().getString(R.string.btnAddMark))) {
            btn.setText(R.string.btnAddAltText);
            mode = 1;
        }
        else{
            btn.setText(R.string.btnAddMark);
            mode = 0;
        }
        imageView.setTag(mode);
    }

    private void notAddingMark(){
        Button btn = findViewById(R.id.btnAddMark);
        btn.setText(R.string.btnAddMark);
        mode = 0;
        imageView.setTag(mode);
    }

    private void save(Mark mark){
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);

        mark.setDevice(DeviceManager.getCurrentDevice());

        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_MARKSAVE_KEY);
        uriBuilder.setMark(mark);
        uriBuilder.setProject(report.getDrawingref().getProject());
        uriBuilder.setPage(report.getPages().get(currentPage));
        restTemplateHelper.execute(uriBuilder);
    }

    private void remove(Mark mark){
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(this);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_MARKREMOVE_KEY);
        uriBuilder.setMark(mark);
        uriBuilder.setProject(report.getDrawingref().getProject());
        uriBuilder.setPage(report.getPages().get(currentPage));
        restTemplateHelper.execute(uriBuilder);
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

    private void nextPage(){
        if(currentPage < pageCount - 1) {
            originalBitmap = WQCDocumentHandler.pageLoad(++currentPage, filePath, getResources());
            currentBitmap = WQCDocumentHandler.bitmapCopy(originalBitmap);
            updatePointsDrawing();
        }
        updateButtonState();
    }

    private void previousPage(){
        if(currentPage > 0) {
            originalBitmap = WQCDocumentHandler.pageLoad(--currentPage, filePath, getResources());
            if(originalBitmap != null){
                updateImageView(currentBitmap);
                currentBitmap = WQCDocumentHandler.bitmapCopy(originalBitmap);
            }
            updatePointsDrawing();
        }
        updateButtonState();
    }

    private void updateButtonState(){
        findViewById(R.id.btnNext).setEnabled(!(currentPage == pageCount - 1));
        findViewById(R.id.btnPrevious).setEnabled(!(currentPage == 0));
        findViewById(R.id.btnUndo).setEnabled(canUndo());
    }

    private void updateImageView(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    private void addMark(float[] touchPoint){
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        Mark mark = new Mark();
        mark.setX(touchPoint[0]);
        mark.setY(touchPoint[1]);
        mark.setDevice(DeviceManager.getCurrentDevice());
        markList.add(mark);
        notAddingMark();
        updateButtonState();
        toggleControls(false);
        save(mark);
    }

    private void undo(){
        Mark mark;
        mark = getLastContextMark();
        if(mark != null) {
            updateButtonState();
            notAddingMark();
            toggleControls(false);
            remove(mark);
        }
    }

    private Mark getLastContextMark(){
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        Mark mark;
        if(markList != null && markList.size() > 0){
            for(int i = markList.size() - 1; i >= 0; i--) {
                mark = markList.get(i);
                if (mark.getDevice().getRole().equals(DeviceManager.getCurrentDevice().getRole())) {
                    return mark;
                }
            }
        }
        return null;
    }

    private void updatePointsDrawing(){
        List<Mark> markList = report.getPages().get(currentPage).getMarks();
        if(markList != null) {
            currentBitmap = WQCDocumentHandler.updatePointsDrawing(markList, originalBitmap, getResources());
            updateImageView(currentBitmap);
        }
        else{
            updateImageView(originalBitmap);
        }
    }

    private boolean canUndo(){
        List<Mark> marks = report.getPages().get(currentPage).getMarks();
        for(int i = 0; i < marks.size(); i++){
            if(marks.get(i).getDevice().getRole().equals(DeviceManager.getCurrentDevice().getRole())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onChangeHappened(float[] touchPoint) {
        addMark(touchPoint);
    }

    private void toggleControls(boolean bResume){
        (findViewById(R.id.btnAddMark)).setEnabled(bResume);
        (findViewById(R.id.btnUndo)).setEnabled(canUndo());
        (findViewById(R.id.btnNext)).setEnabled(bResume);
        (findViewById(R.id.btnPrevious)).setEnabled(bResume);
        (findViewById(R.id.btnExit)).setEnabled(bResume);
        if(!bResume) {
            (findViewById(R.id.pbCheckReportEdit)).setVisibility(View.VISIBLE);
        } else {
            (findViewById(R.id.pbCheckReportEdit)).setVisibility(View.INVISIBLE);
        }
    }

    private void dataLoadError(){
        toggleControls(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = getResources().getString(R.string.dataRecoverError);
        builder.setMessage(message);
        builder.setCancelable(false);
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
        toggleControls(true);
        if(result != null) {
            if (!result.equals(App.getContext().getResources().getString(R.string.drawingLockedMessage))) {
                if (requestCode.equals(REST_MARKSAVE_KEY)) {
                    List<Mark> marks = report.getPages().get(currentPage).getMarks();
                    marks.get(marks.size() - 1).setId(JsonHandler.toMark(result).getId());
                } else if (requestCode.equals(REST_MARKREMOVE_KEY)) {
                    List<Mark> markList = report.getPages().get(currentPage).getMarks();
                    markList.remove(getLastContextMark());
                } else if (requestCode.equals(REST_QRPROJECTLOAD_KEY)) {
                    Project project = JsonHandler.toProject(result);
                    report = (CheckReport) project.getDrawingRefs().get(report.getDrawingref().getId()).getReports().get(report.getId());
                    toggleControls(true);
                }
            } else {
                toggleControls(false);
                projectLoad();
                dataLoadError(App.getContext().getResources().getString(R.string.drawingLockedMessage));
            }
            updatePointsDrawing();
        } else {
            dataLoadError();
        }

    }
}
