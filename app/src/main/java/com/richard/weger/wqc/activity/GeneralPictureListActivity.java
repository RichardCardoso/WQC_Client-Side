package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.GeneralPicturePreviewAdapter;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.dto.FileDTO;
import com.richard.weger.wqc.helper.ImageHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.service.AsyncMethodExecutor;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.GeneralPictureDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.GENPICTURE_CAPTURE_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.REQUEST_IMAGE_CAPTURE_ACTION;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_CONFIGLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;

public class GeneralPictureListActivity extends Activity implements RestTemplateHelper.RestResponseHandler, GeneralPicturePreviewAdapter.PictureTapHandler{

    List<GeneralPictureDTO> picsList = new ArrayList<>();
    Project project;
    private GeneralPicturePreviewAdapter adapter;
    List<FileRestTemplateHelper> queue = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_pictures_list);
        ConfigurationsManager.loadServerConfig(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        toggleControls(false);
        if(resultCode == RESULT_OK) {
            if (requestCode == GENPICTURE_CAPTURE_SCREEN_ID) {
                ConfigurationsManager.loadServerConfig(this);
//                if(data.hasExtra("pics")) {
//                    List<String> pics = data.getStringArrayListExtra("pics");
//                    if (pics != null && pics.size() > 0) {
//                        addFilesToList(pics, true);
//                        toggleLoading(false);
//                        return;
//                    }
//                }
            } else if(requestCode == REQUEST_IMAGE_CAPTURE_ACTION) {
                if(data.hasExtra("newPictures")) {
                    List<String> newPictures = data.getStringArrayListExtra("newPictures");
                    if(newPictures.size() > 0) {
                        proccessNewPictures(newPictures);
                        return;
                    }
                }
            }
        }
        toggleControls(true);
    }

    private void proccessNewPictures(List<String> newPictures){
        Intent intent = new Intent(GeneralPictureListActivity.this, GeneralPictureCaptureActivity.class);

        intent.putStringArrayListExtra("newPictures", new ArrayList<>(newPictures));
        intent.putExtra("project", project);

        startActivityForResult(intent, GENPICTURE_CAPTURE_SCREEN_ID);
    }

    private void inflateActivityLayout(){
        setContentView(R.layout.activity_general_pictures_list);
        toggleControls(false);
        setListeners();
    }

    private void setListeners(){
        (findViewById(R.id.backButton)).setOnClickListener(v -> finishAndRemoveTask());
        (findViewById(R.id.takeButton)).setOnClickListener(v -> takePicture());
    }

    private void takePicture(){
        ImageHelper.takePicture(this);
    }

    private void addFileToList(String fileName){
        List<GeneralPictureDTO> pics = new ArrayList<>(picsList);
        pics.add(new GeneralPictureDTO(fileName, false));
        pics.sort(this::compare);
        picsList.clear();
        picsList.addAll(pics);
        updateRecyclerView();
    }

    private void addFilesToList(Set<String> pics){
        List<GeneralPictureDTO> items = pics.stream().map(p -> new GeneralPictureDTO(p, false)).collect(Collectors.toList());
        items.sort(this::compare);

        picsList.clear();
        picsList.addAll(items);
        updateRecyclerView();
    }

    private int compare(GeneralPictureDTO t0, GeneralPictureDTO t1){
        String id0, id1;
        id0 = extractIndex(t0);
        id1 = extractIndex(t1);
        return id1.compareTo(id0);
    }

    private String extractIndex(GeneralPictureDTO pic){
        String n0, ret;
        int iWork;
        n0 =  pic.getFileName();
        n0 = n0.substring(n0.indexOf("QP") + 2, n0.lastIndexOf("."));
        iWork = Integer.valueOf(n0);
        ret = String.format(getResources().getConfiguration().getLocales().get(0), "%05d", iWork);
        return ret;
    }

    private void updateRecyclerView(){
        RecyclerView view = findViewById(R.id.recyclerview);
        if(view != null) {
            if (adapter == null) {
                adapter = new GeneralPicturePreviewAdapter(StringHelper.getPicturesFolderPath(project), picsList, this, false);
            }
            GridLayoutManager grid = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
            view.setLayoutManager(grid);
            view.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            toggleControls(true);
        }
    }

    @Override
    public void RestTemplateCallback(AbstractResult result) {
        if(result instanceof ErrorResult) {
            ErrorResult err = ResultService.getErrorResult(result);
            ErrorResponseHandler.handle(err, this, this::finishAndRemoveTask);
        } else {
            switch (result.getRequestCode()){
                case REST_CONFIGLOAD_KEY:
                    ParamConfigurations conf = ResultService.getSingleResult(result, ParamConfigurations.class);
                    ConfigurationsManager.setServerConfig(conf);
                    ProjectHelper.projectLoad(this, true);
                    break;
                case REST_QRPROJECTLOAD_KEY:
                    project = ResultService.getSingleResult(result, Project.class);
                    ProjectHelper.getGenPicturesList(this);
                    break;
                case REST_GENPICTURESREQUEST_KEY:
                    Set<FileDTO> pictures = new HashSet<>(ResultService.getMultipleResult(result, FileDTO.class));
                    inflateActivityLayout();
                    addFilesToList(new HashSet<>(pictures.stream().map(FileDTO::getFileName).collect(Collectors.toList())));
                    toggleLoading(true);
                    AsyncMethodExecutor.execute(() -> {
                        List<String> toDownload = ProjectHelper.getObsoleteGenPictures(new ArrayList<>(pictures), project);
                        if (toDownload.size() > 0) {
                            ProjectHelper.getGenPictures(toDownload, queue, this);
                        } else {
                            toggleLoading(false);
                        }
                    });
                    break;
                case REST_GENPICTUREDOWNLOAD_KEY:
                    String filename = ResultService.getSingleResult(result, String.class);
                    if(picsList.stream().noneMatch(p -> p.getFileName().equals(filename))) {
                        addFileToList(filename);
                    } else {
                        updateRecyclerView();
                    }

                    if (!ProjectHelper.hasPendingTasks(null, queue, true)) {
                       toggleLoading(false);
                    }
                    break;
            }
        }
    }

    private void toggleLoading(boolean isLoading){
        if(findViewById(R.id.pbGenPic) != null) {
            if (isLoading) {
                findViewById(R.id.pbGenPic).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.pbGenPic).setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void toggleControls(boolean resume) {
        ImageView ivPic = findViewById(R.id.ivPicture);
        if(ivPic != null) {
            ivPic.setClickable(resume);
            ivPic.setEnabled(resume);
        }
        toggleLoading(!resume);
    }

    @Override
    public void onFatalError() {

    }

    @Override
    public void onPictureTap(int position) {
        ImageHelper.showPicture(picsList, position, this);
    }

    @Override
    public void onRemoveRequest(int position) {

    }
}
