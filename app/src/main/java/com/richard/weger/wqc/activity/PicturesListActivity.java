package com.richard.weger.wqc.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.GeneralPictureAdapter;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.JsonHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.richard.weger.wqc.constants.AppConstants.*;
import static com.richard.weger.wqc.helper.LogHelper.writeData;

public class PicturesListActivity extends ListActivity implements GeneralPictureAdapter.ChangeListener, RestTemplateHelper.RestHelperResponse {

    Project project;
    List<String> files = new ArrayList<>();
    List<String> filePaths = new ArrayList<>();
    List<RestTemplateHelper> queue = new ArrayList<>();
    GeneralPictureAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wait);
        Button btn;
        btn = findViewById(R.id.btnExit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        Intent intent = getIntent();
        project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        ProjectHelper.linkReferences(project);

        (new ProjectHelper()).checkForGenPictures(this, project, false);

    }

    private void exit(){
        setResult(RESULT_OK);
        finish();
    }

    private void inflateActivityLayout(){
        setContentView(R.layout.activity_pictures_list);

        Button btn;
        btn = findViewById(R.id.backButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        setListeners();
        setFilesList();
        adapter = new GeneralPictureAdapter(this, files);
        adapter.setChangeListener(this);
        setListAdapter(adapter);

        TextView tv = findViewById(R.id.tvNodata);
        if(files.size() == 0){
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.INVISIBLE);
        }
    }

    private void setFilesList(){
        File folder = new File(StringHelper.getPicturesFolderPath(project));
        for(File f : folder.listFiles()){
            String fName = f.getName();
            if(fName.contains("QP") && !files.contains(fName)){
                files.add(fName);
                filePaths.add(f.getAbsolutePath());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICTURE_VIEWER_SCREEN_ID:
                List<String> takenPictures;
                if(data != null && data.hasExtra(TAKEN_PICTURES_KEY)) {
                    takenPictures = data.getStringArrayListExtra(TAKEN_PICTURES_KEY);
                    for (String picName : takenPictures) {
                        ProjectHelper.generalPictureUpload(this, project, picName.substring(picName.lastIndexOf("/")), queue);
                        findViewById(R.id.takeButton).setVisibility(View.INVISIBLE);
                    }
                }
        }
    }

    private void setListeners(){
        Button btn;
        btn = findViewById(R.id.takeButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PicturesListActivity.this, PictureViewerActivity.class);
                intent.putExtra(PICTURE_CAPTURE_MODE, GENERAL_PICTURE_MODE);
                intent.putExtra(PROJECT_KEY, project);
                startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
            }
        });

        btn = findViewById(R.id.backButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
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

    private void addFileToList(String result){
        if(!files.contains(result)){
            if(!result.endsWith(".jpg")){
                result = result.concat(".jpg");
            }
            files.add(result);
            filePaths.add(StringHelper.getPicturesFolderPath(project).concat(result));
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void RestTemplateCallback(String requestCode, String result) {
        if(result != null) {
            switch (requestCode) {
                case REST_GENPICTUREUPLOAD_KEY:
                    addFileToList(result);
                    break;
                case REST_GENPICTUREDOWNLOAD_KEY:
                    addFileToList(result);
                    if (!ProjectHelper.hasPendingTasks(queue, true) && adapter == null) {
                        inflateActivityLayout();
                    }
                    break;
                case REST_GENPICTURESREQUEST_KEY:
                    writeData("Got existing general pictures list from server");
                    List<String> pictures = JsonHelper.toList(result, String.class);
                    if (pictures.size() > 0) {
                        ProjectHelper projectHelper = new ProjectHelper();
                        projectHelper.getGenPictures(this, pictures, queue, project);
                        if(!ProjectHelper.hasPendingTasks(queue, true)){
                            inflateActivityLayout();
                        }
                    } else {
                        inflateActivityLayout();
                    }
                    break;
            }
        } else {
            Toast.makeText(this, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
        }
    }
}
