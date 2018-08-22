package com.richard.weger.wegerqualitycontrol.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Configurations;
import com.richard.weger.wegerqualitycontrol.domain.ControlCardReport;
import com.richard.weger.wegerqualitycontrol.domain.Item;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;
import com.richard.weger.wegerqualitycontrol.util.AppConstants;
import com.richard.weger.wegerqualitycontrol.util.AsyncFromServerToLocalFile;
import com.richard.weger.wegerqualitycontrol.util.ConfigurationsManager;
import com.richard.weger.wegerqualitycontrol.util.FileHandler;
import com.richard.weger.wegerqualitycontrol.util.PermissionsManager;
import com.richard.weger.wegerqualitycontrol.util.QrTextHandler;
import com.richard.weger.wegerqualitycontrol.util.AsyncSmbFilesList;
import com.richard.weger.wegerqualitycontrol.util.StringHandler;
import com.richard.weger.wegerqualitycontrol.util.JsonHandler;
import com.richard.weger.wegerqualitycontrol.util.WQCPointF;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jcifs.smb.SmbFile;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public class ProjectMain
        extends Activity
        implements AsyncSmbFilesList.AsyncSmbFilesListResponse,
                    AsyncFromServerToLocalFile.AsyncSmbInStreamResponse {

    SharedPreferences mPrefs;
    Configurations conf = new Configurations();
    Project project = new Project();
    Locale locale ;
    private Map<String, CheckBox> checkBoxMap = new HashMap<>();
    private boolean foundConstructionPath = false;
    private boolean foundDatasheetPath = false;
    private Map<String, String> mapValues;

    @Override
    public void onPause(){
        super.onPause();
        save();
    }

    @Override
    public void onResume(){
        super.onResume();
        updatePendingItemsInfo();
        save();
    }

    private void save(){
        if(project != null) {
            if (!project.getNumber().equals(""))
                JsonHandler.jsonProjectSave(this, StringHandler.generateFileName(project), project);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_main);
        if(project.getNumber().equals("")) {
            startSourceSelection();
        }
        setListeners();
        locale = getResources().getConfiguration().locale;
        fillCheckBoxMap();
        init();
    }

    private void init(){
        PermissionsManager permissionsManager = new PermissionsManager();
        Configurations configurations = ConfigurationsManager.loadConfig(this);
        if(configurations == null){
            ConfigurationsManager.saveConfig(conf, this);
        }
        else{
            conf = ConfigurationsManager.loadConfig(this);
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!permissionsManager.checkPermission(EXTERNAL_DIR_PERMISSION, this, false)){
            permissionsManager.askPermission(EXTERNAL_DIR_PERMISSION, this);
        }
    }

    private void fillCheckBoxMap(){
        checkBoxMap.put(ControlCardReport.class.getName(), (CheckBox) findViewById(R.id.chkControlCardReport));
    }

    private void setListeners(){
        Button button;

        button = findViewById(R.id.btnControlCardReport);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectMain.this, ReportEditActivity.class);
                intent.putExtra(PROJECT_KEY, project);
                intent.putExtra(REPORT_KEY, project.getReportList().get(CONTROL_CARD_REPORT_ID));
                startActivityForResult(intent, CONTROL_CARD_REPORT_EDIT_SCREEN_KEY);
            }
        });

        button = findViewById(R.id.btnProjectFinish);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updatePendingItemsInfo() <= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProjectMain.this);
                    builder.setTitle(R.string.projectFinishButton);
                    builder.setMessage(R.string.projectFinishConfirmation);
                    builder.setPositiveButton(R.string.yesTAG, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ProjectMain.this, ProjectFinishActivity.class);
                            intent.putExtra(CONTROL_CARD_REPORT_FILE_KEY,
                                    StringHandler.generateFileName(project, "xls"));
                            intent.putExtra(MAP_VALUES_KEY, (HashMap) mapValues);
                            intent.putExtra(PROJECT_KEY, project);
                            startActivityForResult(intent, PROJECT_FINISH_SCREEN_ID);
                        }
                    });
                    builder.setNegativeButton(R.string.noTag, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProjectMain.this);
                    builder.setTitle(R.string.unableToProcced);
                    builder.setMessage(R.string.unfinishedReportsMessage);
                    builder.setPositiveButton(R.string.okTag, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
            }
        });

        button = findViewById(R.id.btnDrawingCheck);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                documentCheck(CONSTRUCTION_PATH_KEY);
                if(!foundConstructionPath &&
                        project.getDrawingList().get(0).getOriginalFileLocalPath().equals("")) {
                    handlePaths(CONSTRUCTION_PATH_KEY);
                    findViewById(R.id.btnDrawingCheck).setEnabled(false);
                    Toast.makeText(
                            ProjectMain.this,
                            R.string.beginServerConnectionMessage,
                            Toast.LENGTH_LONG).show();
                } else {
                    documentCheck(CONSTRUCTION_PATH_KEY,
                            project.getDrawingList().get(0).getOriginalFileLocalPath());
                }
            }
        });

        button = findViewById(R.id.btnDatasheetCheck);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                documentCheck(TECHNICAL_PATH_KEY);
                if(!foundDatasheetPath &&
                        project.getDrawingList().get(0).getDatasheet().getOriginalFileLocalPath().equals("")){
                    handlePaths(TECHNICAL_PATH_KEY);
                    findViewById(R.id.btnDatasheetCheck).setEnabled(false);
                    Toast.makeText(
                            ProjectMain.this,
                            R.string.beginServerConnectionMessage,
                            Toast.LENGTH_LONG).show();
                } else {
                    documentCheck(TECHNICAL_PATH_KEY,
                            project.getDrawingList().get(0).getDatasheet().getOriginalFileLocalPath());
                }
            }
        });
    }

    private void startSourceSelection(){
        Bundle b = new Bundle();

        startWaitingResult(null, ProjectMain.this, SourceSelectionActivity.class,
                SOURCE_SELECTION_SCREEN_KEY);
    }

    public void startWaitingResult(Bundle b, Context packageContext, Class cls,
                                   int requestCode){
        Intent intent = new Intent(packageContext, cls);
        if(b != null){
            intent.putExtras(b);
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            Bundle b = data.getExtras();
            if(b != null){
                switch (requestCode) {
                    case SOURCE_SELECTION_SCREEN_KEY: {
                        String sourceCode;
                        sourceCode = b.getString(SOURCE_CODE_KEY);
                        if (sourceCode != null) {
                            handleSourceString(b.getString(SOURCE_CODE_KEY), b);
                        }
                        break;
                    }
                    case CONTROL_CARD_REPORT_EDIT_SCREEN_KEY: {
                        Report report = (Report) b.getSerializable(REPORT_KEY);
                        if (report != null) {
                            project.getReportList().set(CONTROL_CARD_REPORT_ID, report);
                        }
                        break;
                    }
                    case CONFIG_SCREEN_KEY: {
                        conf = ConfigurationsManager.loadConfig(this);
                    }
                    case DOCUMENT_MARK_SCREEN: {
                        Map<Integer, List<WQCPointF>> hashPoints = (HashMap) b.getSerializable(DOCUMENT_HASH_POINTS_KEY);
                        String documentType = b.getString(DOCUMENT_TYPE_KEY);

                        if(documentType.equals(CONSTRUCTION_PATH_KEY))
                            project.getDrawingList().get(0).setHashPoints(hashPoints);
                        else if(documentType.equals(TECHNICAL_PATH_KEY))
                            project.getDrawingList().get(0).getDatasheet().setHashPoints(hashPoints);
                    }
                }
            }
        }
        else{
            if(requestCode == SOURCE_SELECTION_SCREEN_KEY) {
                if(data != null){
                    int closeReason = data.getIntExtra(CLOSE_REASON, -1);
                    if(closeReason != -1){
                        if(closeReason == CLOSE_REASON_USER_FINISH){
                            finish();
                            return;
                        }
                    }
                }
                startSourceSelection();
            }
        }
    }

    private void handleSourceString(String sourceCode, Bundle b){
        String qrText = "";
        QrTextHandler qrTextHandler;
        String fileName;
        switch (sourceCode) {
            case SOURCE_CODE_QR: {
                qrText = b.getString(AppConstants.QR_CODE_KEY);
                break;
            }
            case SOURCE_CODE_CONTINUE: {
                fileName = b.getString(CONTINUE_CODE_KEY);
                if (!loadSavedFile(fileName)){
                    startSourceSelection();
                    return;
                }
                qrText = StringHandler.createQrText(project);
                break;
            }
        }
        qrTextHandler = new QrTextHandler(this);
        if(qrText.equals("")) {
            startSourceSelection();
            return;
        }
        mapValues = qrTextHandler.execute(qrText);
        if(mapValues != null) {
            setFields(mapValues);
        }
        else{
            Toast.makeText(this, R.string.invalidQrCodeString, Toast.LENGTH_LONG).show();
            startSourceSelection();
        }
    }

    private void handlePaths(String documentKey){
        AsyncSmbFilesList asyncSmbFilesList = new AsyncSmbFilesList(this, conf);
        switch(documentKey){
            case CONSTRUCTION_PATH_KEY: {
                asyncSmbFilesList.execute(mapValues.get(CONSTRUCTION_PATH_KEY), CONSTRUCTION_PATH_KEY);
                break;
            }
            case TECHNICAL_PATH_KEY: {
                asyncSmbFilesList.execute(mapValues.get(AppConstants.TECHNICAL_PATH_KEY), TECHNICAL_PATH_KEY);
                break;
            }
        }
    }

    private boolean loadSavedFile(String fileName){
        project = (new JsonHandler()).jsonProjectLoad(this, fileName);
        if(project == null) {
            Toast.makeText(this,R.string.dataRecoverError,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void setFields(Map<String, String> mapValues){
        String projectNumber = mapValues.get(PROJECT_NUMBER_KEY);
        String drawingNumber = mapValues.get(DRAWING_NUMBER_KEY);
        String partNumber = mapValues.get(PART_NUMBER_KEY);

        project.setNumber(projectNumber);
        ((TextView)findViewById(R.id.tvProjectInfo)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.projectNumberPrefix), projectNumber));

        project.getDrawingList().get(0).setNumber(Integer.valueOf(drawingNumber));
        ((TextView)findViewById(R.id.tvReportType)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.drawingNumberPrefix), drawingNumber));

        project.getDrawingList().get(0).getPart().get(0).setNumber(Integer.valueOf(partNumber));
        ((TextView)findViewById(R.id.tvPartNumber)).setText(String.format(locale, "%s%s",
                getResources().getString(R.string.partNumberPrefix), partNumber));

        for(Report r :project.getReportList()){
            r.getDrawing().setNumber(Integer.valueOf(drawingNumber));
            r.getPart().setNumber(Integer.valueOf(partNumber));
            for(Item i : r.getItemList()){
                if(i.getPicture().getFilePath() == null){
                    i.getPicture().setFilePath("");
                }
                if(i.getPicture().getFilePath().equals("")) {
                    i.getPicture().setFilePath(StringHandler.generatePictureName(project, i, r));
                }
            }
        }
    }

    private void handleFileList(SmbFile[] fileList, String entryData){
        if(fileList == null){
            Toast.makeText(this, R.string.smbConnectError, Toast.LENGTH_LONG).show();
            //startSourceSelection();
            return;
        }

        for (SmbFile f : fileList) {
            String fName = f.getName();
            String fCode = fName.substring(0, 4);
            String fExtension = fName.substring(fName.length() - 4, fName.length());
            if(FileHandler.fileNameMatches(entryData, conf, fCode, fExtension)){
                String localPath = StringHandler.generateProjectFolderName(getExternalFilesDir(null), project).concat("Originals/");
                File localFolder, localFile;
                localFolder = new File(localPath);
                if(!localFolder.exists()){
                    localFolder.mkdirs();
                }
                localFile = new File(localPath.concat(f.getName()));
                AsyncFromServerToLocalFile asyncFromServerToLocalFile = new AsyncFromServerToLocalFile(this, conf);
                asyncFromServerToLocalFile.execute(f, entryData, localFile);
            }
        }
    }

    private int updatePendingItemsInfo(){
        int pendingItems, totalPendingItems = 0;
        if(project != null) {
            if (project.getReportList() != null) {
                for (Report r : project.getReportList()) {
                    if (r instanceof ControlCardReport) {
                        CheckBox checkBox = checkBoxMap.get(r.getClass().getName());
                        pendingItems = r.getPendingItemsCount();
                        totalPendingItems += pendingItems;
                        checkBox.setChecked(pendingItems == 0);
                        findViewById(R.id.btnProjectFinish).setEnabled(pendingItems == 0);
                    }
                }
            }
        }
        return totalPendingItems;
    }

    private void documentCheck(String documentKey, String filePath){
        Intent intent;
//        filePath = WQCDocumentHandler.getFilePath(documentKey, conf, mapValues);
        if(filePath == null){
            Toast.makeText(this, R.string.invalidDocumentCodesMessage, Toast.LENGTH_LONG).show();
            return;
        }
        intent = new Intent(ProjectMain.this, DocumentMarkerActivity.class);
        intent.putExtra(FILE_PATH_KEY, filePath);
        if(documentKey.equals(CONSTRUCTION_PATH_KEY))
            intent.putExtra(DOCUMENT_HASH_POINTS_KEY, (HashMap) project.getDrawingList().get(0).getHashPoints());
        else
            intent.putExtra(DOCUMENT_HASH_POINTS_KEY, (HashMap) project.getDrawingList().get(0).getDatasheet().getHashPoints());
        intent.putExtra(DOCUMENT_TYPE_KEY, documentKey);
        startActivityForResult(intent, DOCUMENT_MARK_SCREEN);
    }


    @Override
    public void AsyncSmbFilesListResponseCallback(SmbFile[] fileList, String entryData) {
        handleFileList(fileList, entryData);
    }

    @Override
    public void AsyncSmbInStreamCallback(boolean bResult, String entryData, String localFilePath) {
        if(bResult){
            switch(entryData){
                case CONSTRUCTION_PATH_KEY:
                    project.getDrawingList().get(0).setOriginalFileLocalPath(localFilePath);
                    foundConstructionPath = true;
                    break;
                case TECHNICAL_PATH_KEY:
                    project.getDrawingList().get(0).getDatasheet().setOriginalFileLocalPath(localFilePath);
                    foundDatasheetPath = true;
                    break;
            }
            documentCheck(entryData, localFilePath);
        }
        else{
            Toast.makeText(this, R.string.smbConnectError, Toast.LENGTH_SHORT).show();
        }
        switch(entryData){
            case CONSTRUCTION_PATH_KEY:
                findViewById(R.id.btnDrawingCheck).setEnabled(true);
                break;
            case TECHNICAL_PATH_KEY:
                findViewById(R.id.btnDatasheetCheck).setEnabled(true);
                break;
        }
    }
}
