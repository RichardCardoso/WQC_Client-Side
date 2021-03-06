package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.GeneralPicturePreviewAdapter;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ImageHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileRequest;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.service.AsyncMethodExecutor;
import com.richard.weger.wqc.service.FileRequestParametersResolver;
import com.richard.weger.wqc.util.GeneralPictureDTO;
import com.richard.weger.wqc.util.GeneralPicturesProcessorScheduler;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_VIEWER_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.REQUEST_IMAGE_CAPTURE_ACTION;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.util.App.getStringResource;

public class GeneralPictureCaptureActivity extends Activity implements
        GeneralPicturePreviewAdapter.PictureTapHandler, RestTemplateHelper.RestResponseHandler, GeneralPicturesProcessorScheduler.GeneralPicturesProcessorListener {

    List<GeneralPictureDTO> picsList = new ArrayList<>();
    private GeneralPicturePreviewAdapter adapter;
    List<FileRestTemplateHelper> uploadList = new ArrayList<>();
    Project project;
    int uploadCount;
    List<AsyncMethodExecutor> processesList = new ArrayList<>();

    GeneralPicturesProcessorScheduler ex;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_picture_capture);

        Intent intent = getIntent();

        project = (Project) intent.getSerializableExtra("project");
        ProjectHelper.linkReferences(project);

        final List<String> newPictures = intent.getStringArrayListExtra("newPictures");
        handleNewPictures(newPictures, new ArrayList<>());

        setListeners();
        toggleControls(true);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void recyclerViewSetup() {
        toggleControls(false);

        RecyclerView view = findViewById(R.id.recyclerview);
        if(adapter == null){
            adapter = new GeneralPicturePreviewAdapter(StringHelper.getPicturesFolderPath(project), picsList, this, true);
        }
        GridLayoutManager grid = new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false);
        view.setLayoutManager(grid);
        view.setAdapter(adapter);

        updateRecyclerView();
    }

    private void updateRecyclerView() {
        updateRecyclerView(false);
    }

    private void updateRecyclerView(boolean disableRemoveIcon){

        if(adapter == null) {
            recyclerViewSetup();
        }

        if(picsList.size() == 0) {
            (findViewById(R.id.uploadButton)).setVisibility(View.INVISIBLE);
            (findViewById(R.id.backButton)).setOnClickListener(view -> close());
        } else {
            (findViewById(R.id.uploadButton)).setVisibility(View.VISIBLE);
            setListeners();
        }

        adapter.setCanRemove(!disableRemoveIcon);

        adapter.notifyDataSetChanged();
        toggleControls(true);
    }

    private void setCloseResult() {
        Intent ret = new Intent();
        List<String> uploadedPics = picsList.stream()
                .filter(GeneralPictureDTO::isProcessed)
                .map(GeneralPictureDTO::getFileName)
                .collect(Collectors.toList());
        ret.putStringArrayListExtra("pics", new ArrayList<>(uploadedPics));
        setResult(RESULT_OK, ret);
        finish();
    }

    private void setListeners(){
        (findViewById(R.id.backButton)).setOnClickListener(v ->
                AlertHelper.showMessage(null,
                        getStringResource(R.string.beginUploadQuestion),
                        getStringResource(R.string.yesTAG),
                        getStringResource(R.string.noTag),
                        this::beginUpload, this::close, this)
        );
        (findViewById(R.id.takeButton)).setOnClickListener(v -> {
            (findViewById(R.id.takeButton)).setEnabled(false);
            takePicture();
        });
        (findViewById(R.id.uploadButton)).setOnClickListener(v ->
                AlertHelper.showMessage(null,
                        getStringResource(R.string.beginUploadQuestion),
                        getStringResource(R.string.yesTAG),
                        getStringResource(R.string.noTag),
                        this::beginUpload, null, this)
        );
    }

    private void setCancelListener() {
        (findViewById(R.id.backButton)).setOnClickListener(v ->
                AlertHelper.showMessage(null,
                        getStringResource(R.string.cancelQuestion),
                        getStringResource(R.string.yesTAG),
                        getStringResource(R.string.noTag),
                        this::close, null, this)
        );
    }

    private void close() {
        toggleControls(false);
        processesList.forEach(p -> p.cancel(true));
        uploadList.forEach(u -> u.cancel(true));
        List<String> nonProcessed = ex.stopAndGetNotProcessedPics();
        nonProcessed.addAll(picsList.stream().filter(x -> !x.isProcessed()).map(GeneralPictureDTO::getFileName).collect(Collectors.toList()));
        nonProcessed.forEach(x -> {
            String fName = StringHelper.getPicturesFolderPath(ProjectHelper.getProject());
            if(!fName.endsWith(File.separator)) {
                fName = fName.concat(File.separator);
            }
            fName = fName.concat(x);
            try {
                Files.delete(new File(fName).toPath());
            } catch (Exception ignored) {}
        });
        setCloseResult();
    }

    public void transformPictures(final List<String> pics){
        setCancelListener();
        if(ex == null) {
            ex = new GeneralPicturesProcessorScheduler(pics, project, this);
            ex.process();
        } else {
            ex.appendAndProccess(pics);
        }
    }


    @Override
    public void onPicturesListProcessFinish() {
        setListeners();
    }

    public void onPictureProcessed(String fileName) {
        updateRecyclerView();
    }

    private void handleNewPictures(List<String> newPictures, List<String> processedPictures){
        newPictures.forEach(n -> picsList.add(new GeneralPictureDTO(ImageHelper.getFinalFilename(n), false)));
        updateRecyclerView();

        List<String> newPicsPath = new ArrayList<>();
        newPictures.stream()
                .filter(s -> !processedPictures.contains(s))
                .forEach(s -> newPicsPath.add(StringHelper.getPicturesFolderPath(ProjectHelper.getProject()) + s));

        if(newPicsPath.size() > 0) {
            transformPictures(newPicsPath);
        }
    }

    private void takePicture(){
        ImageHelper.takePicture(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE_ACTION) {
                List<String> newPictures = new ArrayList<>();
                List<String> processedPictures = new ArrayList<>();
                if(data.hasExtra("newPictures")) {
                    newPictures = data.getStringArrayListExtra("newPictures");
                }
                if(data.hasExtra("processedPictures")) {
                    processedPictures = data.getStringArrayListExtra("processedPictures");
                }
                if(newPictures.size() > 0) {
                    handleNewPictures(newPictures, processedPictures);
                }
            }
        }
        if(requestCode == PICTURE_VIEWER_SCREEN_ID) {
            toggleControls(true);
        }
    }

    private void beginUpload(){
        toggleControls(false);
        setCancelListener();
        uploadCount = 0;
        for(GeneralPictureDTO pic : picsList) {
            pic.setProcessing(true);
            adapter.notifyDataSetChanged();
            String filePath = StringHelper.getPicturesFolderPath(ProjectHelper.getQrCode()) + pic.getFileName();
            String fileName = pic.getFileName();
            if(FileHelper.isValidFile(filePath)) {
                FileRequestParametersResolver res = new FileRequestParametersResolver(REST_GENPICTUREUPLOAD_KEY, this);
                uploadList.add(res.uploadGeneralPicture(fileName, ProjectHelper.getQrCode()));
                uploadCount++;
            } else {
                pic.setError(true);
                updateRecyclerView(true);
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
    public void RestTemplateCallback(AbstractResult result) {
        if(result.getRequestCode().equals(REST_GENPICTUREUPLOAD_KEY)) {
            String filename;
            FileRequest req = (FileRequest) result.getRequest();
            RequestParameter filenameParam = req.getParameters().stream().filter(p -> p.getName().equals("filename")).findFirst().orElse(null);
            if(filenameParam != null){
                filename = filenameParam.getValue();
                GeneralPictureDTO pic = picsList.stream().filter(p -> p.getFileName().equals(filename)).findFirst().orElse(null);
                if(pic != null){
                    if(result instanceof ErrorResult) {
                        pic.setError(true);
                    }
                    pic.setProcessed(true);
                    adapter.notifyDataSetChanged();
                }
            }
            uploadCount--;
        }
        if(uploadCount == 0) {
            AlertHelper.showMessage(getStringResource(R.string.changesSavedMessage),
                    getStringResource(R.string.okTag), this::setCloseResult
            );
            toggleControls(true);
        }
    }

    public void toggleControls(boolean resume) {
        (findViewById(R.id.takeButton)).setEnabled(resume);
        (findViewById(R.id.uploadButton)).setEnabled(resume);
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
        AlertHelper.showMessage(
                getStringResource(R.string.confirmationNeeded),
                getStringResource(R.string.pictureRemoveQuestion),
                getStringResource(R.string.yesTAG),
                getStringResource(R.string.noTag),
                () -> removeFromList(position),
                null, this);
    }

    private void removeFromList(int position) {
        toggleControls(false);
        GeneralPictureDTO p = picsList.get(position);
        FileHelper.fileDelete(StringHelper.getPicturesFolderPath(project).concat(p.getFileName()));
        picsList.remove(position);
        updateRecyclerView();
    }

}
