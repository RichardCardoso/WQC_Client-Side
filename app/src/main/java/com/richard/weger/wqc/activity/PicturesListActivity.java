package com.richard.weger.wqc.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.GeneralPictureAdapter;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.dto.FileDTO;
import com.richard.weger.wqc.helper.ActivityHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.GENERAL_PICTURE_MODE;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_CAPTURE_MODE;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_FILEPATH_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_VIEWER_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.TAKEN_PICTURES_KEY;

public class PicturesListActivity extends ListActivity implements GeneralPictureAdapter.ChangeListener, RestTemplateHelper.RestResponseHandler {

    Project project;
    List<String> files = new ArrayList<>();
    List<String> filePaths = new ArrayList<>();
    List<FileRestTemplateHelper> queue = new ArrayList<>();
    GeneralPictureAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHelper.setWaitingLayout(this);

        Intent intent = getIntent();
        project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        ProjectHelper.linkReferences(project);

        ProjectHelper.getGenPicturesList(this, project, false);

    }

//    private void exit(){
//        if(!ProjectHelper.hasPendingTasks(null, queue, true)) {
//            setResult(RESULT_OK);
//            finish();
//        } else {
//            String message = getResources().getString(R.string.mustWaitCompletion)
//                    .concat("( ")
//                    .concat(getResources().getString(R.string.picturesUploadTag))
//                    .concat(") ");
//            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_MUST_WAIT_WARNING, message, ErrorResult.ErrorLevel.WARNING, getClass());
//            ErrorResponseHandler.handle(err, this, null);
//        }
//    }

    private void inflateActivityLayout(){
        setContentView(R.layout.activity_pictures_list);

        setListeners();
        setFilesList();
        adapterConfig();
        TextView tv = findViewById(R.id.tvNodata);
        if(files.size() == 0){
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.INVISIBLE);
        }
    }

    private void adapterConfig(){
        adapter = new GeneralPictureAdapter(this, files);
        adapter.setChangeListener(this);
        setListAdapter(adapter);
    }

    private void setFilesList(){
        File folder = new File(StringHelper.getPicturesFolderPath(project));
        if(folder.exists() && folder.listFiles().length > 0) {
            for (File f : folder.listFiles()) {
                String fName = f.getName();
                if (fName.contains("QP") && !files.contains(fName) && !fName.contains("_new")) {
                    files.add(fName);
                } else if (fName.contains("_new")){
                    try{
                        f.delete();
                    } catch (Exception ignored){}
                }
            }
        }
        formatFilesList();
    }

    private void formatFilesList(){
        files = files.stream().sorted(this::fileNameCompare)
                .collect(Collectors.toList());
        filePaths = files.stream().map(f -> StringHelper.getPicturesFolderPath(project).concat(f)).collect(Collectors.toList());
        files = files.stream().map(this::getFormattedFileName).collect(Collectors.toList());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICTURE_VIEWER_SCREEN_ID) {
            List<String> takenPictures;
            if (data != null && data.hasExtra(TAKEN_PICTURES_KEY)) {
                takenPictures = data.getStringArrayListExtra(TAKEN_PICTURES_KEY);
                if (takenPictures.size() > 0) {
                    ActivityHelper.disableActivityControls(this,true);
                    findViewById(R.id.takeButton).setVisibility(View.INVISIBLE);
                    findViewById(R.id.pbPicturesList).setVisibility(View.VISIBLE);
                    for (String picName : takenPictures) {
                        ProjectHelper.generalPictureUpload(this, project, picName.substring(picName.lastIndexOf("/")), queue);
                    }
                } else {
                    findViewById(R.id.takeButton).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setListeners(){
        ImageButton btn;

        btn = findViewById(R.id.takeButton);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(PicturesListActivity.this, PictureViewerActivity.class);
            intent.putExtra(PICTURE_CAPTURE_MODE, GENERAL_PICTURE_MODE);
            intent.putExtra(PROJECT_KEY, project);
            startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
        });

        btn = findViewById(R.id.backButton);
        btn.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public void onTouch(int position, View view) {
        Intent intent = new Intent(PicturesListActivity.this, PictureViewerActivity.class);
        intent.putExtra(PICTURE_CAPTURE_MODE, GENERAL_PICTURE_MODE);
        intent.putExtra(PROJECT_KEY, project);
        intent.putExtra(PICTURE_FILEPATH_KEY, filePaths.get(position));
        startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
    }

    private int fileNameCompare(String f1, String f2){
        String n1 = "";
        String n2 = "";
        String s1, s2;

        try {
            s1 = f1.substring(f1.toLowerCase().lastIndexOf("qp") + 2);
            s2 = f2.substring(f2.toLowerCase().lastIndexOf("qp") + 2);
            if(s1.contains(".")){
                s1 = s1.substring(0, s1.indexOf("."));
            }
            if(s2.contains(".")){
                s2 = s2.substring(0, s2.indexOf("."));
            }
            n1 = String.format(getResources().getConfiguration().getLocales().get(0), "%02d", Integer.valueOf(s1));
            n2 = String.format(getResources().getConfiguration().getLocales().get(0), "%02d", Integer.valueOf(s2));
        } catch (Exception ex) {
        }

        return n1.compareTo(n2);
    }


    private String getFormattedFileName(String rawName){
        String ret;
        try {
            ret = rawName.substring(rawName.indexOf("Z"), rawName.indexOf("T"))
                    + " " + rawName.substring(rawName.indexOf("T"), rawName.indexOf("QP"))
                    + " " + rawName.substring(rawName.indexOf("QP"));
            if (ret.contains(".")) {
                ret = ret.substring(0, ret.lastIndexOf("."));
            }
            while(ret.contains("  ")){
                ret = ret.replace("  ", " ");
            }
        } catch (Exception ex){
            return rawName;
        }
        return ret;
    }

    private void addFileToList(String result){
        if(!files.contains(result) && result != null){
            if(!result.endsWith(".jpg")){
                result = result.concat(".jpg");
            }
            files.add(result);
            formatFilesList();
            adapterConfig();

            if(ProjectHelper.hasPendingTasks(null, queue, true)){
                TextView tvMessage = findViewById(R.id.tvMessage);
                if(tvMessage != null){
                    tvMessage.setText(
                            getResources().getString(R.string.retrievingGeneralPicturesTag)
                                    .concat(" - ")
                                    .concat(getResources().getString(R.string.remainingTag, queue.size() - 1))
                    );
                }
            } else {
                ProgressBar pbPicturesList = findViewById(R.id.pbPicturesList);
                if(pbPicturesList != null) {
                    pbPicturesList.setVisibility(View.INVISIBLE);
                    ImageButton btn;
                    btn = findViewById(R.id.takeButton);
                    btn.setVisibility(View.VISIBLE);
                }
                if(adapter == null) {
                    inflateActivityLayout();
                }
            }
            if(adapter != null && files.size() == 1){
                TextView tv = findViewById(R.id.tvNodata);
                if(files.size() == 0){
                    tv.setVisibility(View.VISIBLE);
                } else {
                    tv.setVisibility(View.INVISIBLE);
                }
            }

        }
    }

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        if(result instanceof SuccessResult) {
            switch (result.getRequestCode()) {
                case REST_GENPICTUREUPLOAD_KEY:
                case REST_GENPICTUREDOWNLOAD_KEY:
                    String filename = ResultService.getSingleResult(result, String.class);
                    addFileToList(filename);
                    break;
                case REST_GENPICTURESREQUEST_KEY:
                    List<FileDTO> pictures = ResultService.getMultipleResult(result, FileDTO.class);
                    if (pictures.size() > 0) {
                        ProjectHelper.getGenPictures(this, pictures, queue, project);
                        if (!ProjectHelper.hasPendingTasks(null, queue, true)) {
                            inflateActivityLayout();
                        }
                    } else {
                        inflateActivityLayout();
                    }
                    break;
            }
        } else {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, null);
        }
    }

    @Override
    public void toggleControls(boolean resume) {

    }

    @Override
    public void onFatalError() {

    }
}
