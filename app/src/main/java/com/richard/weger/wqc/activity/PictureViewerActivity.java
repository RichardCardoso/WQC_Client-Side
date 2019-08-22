package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.view.View;
import android.widget.ImageView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ImageHelper;
import com.richard.weger.wqc.helper.MessageboxHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;

import uk.co.senab.photoview.PhotoViewAttacher;

import static com.richard.weger.wqc.appconstants.AppConstants.GENERAL_PICTURE_MODE;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_ID_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_PICTURE_MODE;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURES_AUTHORITY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_CAPTURE_MODE;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_FILEPATH_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REPORT_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REQUEST_IMAGE_CAPTURE_ACTION;
import static com.richard.weger.wqc.appconstants.AppConstants.SDF;
import static com.richard.weger.wqc.appconstants.AppConstants.TAKEN_PICTURES_KEY;

public class PictureViewerActivity extends Activity{

    Item item = new Item();
    int position = -1;
    PhotoViewAttacher mAttacher;
    ImageView imageView;
    String futurePath = "";
    Project project;
    Report report;
    String mode;
    ArrayList<String> takenPictures = new ArrayList<>();
    Logger logger;

    @Override
    public void onBackPressed(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = LoggerManager.getLogger(getClass());

        setContentView(R.layout.activity_picture_viewer);

        imageView = findViewById(R.id.ivItem);

        Intent intent = getIntent();
        if(intent != null){

            project = (Project) intent.getSerializableExtra(PROJECT_KEY);
            ProjectHelper.linkReferences(project);

            mode = intent.getStringExtra(PICTURE_CAPTURE_MODE);
            if(mode.equals(ITEM_PICTURE_MODE)) {
                report = (Report) intent.getSerializableExtra(REPORT_KEY);
                item = (Item) intent.getSerializableExtra(ITEM_KEY);
                position = intent.getIntExtra(ITEM_ID_KEY, -1);
                if(position == -1){
                    String message = getResources().getString(R.string.invalidItemIdError).concat("\n(").concat(getResources().getString(R.string.invalidEntityError));
                    ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_INVALID_ITEM_ID_EXCEPTION, message, ErrorResult.ErrorLevel.SEVERE, getClass());
                    ErrorResponseHandler.handle(err, this, this::resultCanceled);
                    return;
                }

                if(item != null){
                    if (item.getPicture() != null && item.getPicture().getFileName() != null){
                        File file = new File(StringHelper.getPicturesFolderPath(project).concat(item.getPicture().getFileName()));
                        if(file.exists() && FileHelper.isValidFile(file.getPath())){
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            if(bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    }
                }
            } else {
                if(intent.hasExtra(PICTURE_FILEPATH_KEY)){
                    String filePath = intent.getStringExtra(PICTURE_FILEPATH_KEY);
                    if(filePath != null) {
                        findViewById(R.id.btnTakeNew).setVisibility(View.INVISIBLE);
                        File file = new File(filePath);
                        if (file.exists() && FileHelper.isValidFile(file.getPath())) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            }
                        } else {
                            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_FILE_ACCESS_EXCEPTION, getResources().getString(R.string.fileNotFoundMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
                            ErrorResponseHandler.handle(err, this, () ->{
                                setResult(RESULT_OK);
                                finish();
                            });
                            return;
                        }
                    } else {
                        takePicture();
                    }
                } else {
                    takePicture();
                }
            }
        }

        if(DeviceHelper.isOnlyRole("te") || (report != null && report.isFinished())){
            findViewById(R.id.btnTakeNew).setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.btnTakeNew).setOnClickListener(v -> takePicture());
        findViewById(R.id.btnCancel).setOnClickListener(v -> resultCanceled());

        mAttacher = new PhotoViewAttacher(imageView);
    }

    private void resultCanceled(){
        setResult(RESULT_CANCELED);
        finish();
    }
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                String message = getResources().getString(R.string.photoCaptureError).concat("\n(").concat(getResources().getString(R.string.tryAgainLaterMessage));
                ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_PHOTO_CAPTURE_SETTING_EXCEPTION, message, ErrorResult.ErrorLevel.SEVERE, getClass());
                ErrorResponseHandler.handle(err, this, null);
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        PICTURES_AUTHORITY,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_ACTION);
            }
        }
    }

    private File createImageFile() {
        // Create an image file name
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName;
        if (mode.equals(ITEM_PICTURE_MODE)) {
            imageFileName = item.getPicture().getFileName();
        } else {
            imageFileName = project.getReference()
                    .concat("Z").concat(String.valueOf(project.getDrawingRefs().get(0).getDnumber()))
                    .concat("T").concat(String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()))
                    .concat("QP").concat(String.valueOf(ProjectHelper.getCurrentPicNumber(project)))
                    .concat(".jpg");
        }
        String folderPath = StringHelper.getProjectFolderPath(project);
        if (folderPath != null){
            folderPath = folderPath.concat("Pictures/");
        }
        File storageDir = new File(folderPath);
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }

        if(!imageFileName.contains(folderPath)){
            imageFileName = folderPath.concat(imageFileName);
        }

        File image;
        try {
            image = new File(imageFileName.replace(".jpg","_new.jpg"));
            if(image.exists()){
                image.delete();
            }
            image.createNewFile();
        } catch (Exception e) {
            try{
                imageFileName = imageFileName.substring(
                        imageFileName.lastIndexOf('/') + 1,
                        imageFileName.lastIndexOf('.'));
                        if(imageFileName.length() - imageFileName.lastIndexOf('-') > 15)
                            imageFileName = imageFileName.substring(0,
                                    imageFileName.lastIndexOf('-') + 9);
                image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
            }
            catch (IOException e2) {
                logger.warning(e.toString());
                return null;
            }
        }
        // Save a file: path for use with ACTION_VIEW intents
        futurePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            putTimeStamp();

            try {
                String finalPath, compressedPath;

                compressedPath = (new ImageHelper()).compressImage(futurePath);
                FileHelper.fileDelete(futurePath);

                finalPath = futurePath.replace("_new","");
                FileHelper.fileCopy(new File(compressedPath), new File(finalPath));
                FileHelper.fileDelete(compressedPath);

                futurePath = finalPath;
                takenPictures.add(futurePath);

            } catch (Exception ex){
                logger.warning(ex.toString());
            }

            if(mode.equals(GENERAL_PICTURE_MODE)) {
                MessageboxHelper.showMessage(this, "",
                        getResources().getString(R.string.getMorePicturesTag),
                        getResources().getString(R.string.yesTAG),
                        getResources().getString(R.string.noTag),
                        this::takePicture, this::finishTakingPictures);
            } else {
                finishTakingPictures();
            }
        } else {
            if(mode.equals(GENERAL_PICTURE_MODE)){
                MessageboxHelper.showMessage(this, "",
                        getResources().getString(R.string.getMorePicturesTag),
                        getResources().getString(R.string.yesTAG),
                        getResources().getString(R.string.noTag),
                        this::takePicture, this::finishTakingPictures);
            } else {
                File file = new File(futurePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void finishTakingPictures(){
        Intent intent = new Intent();
        if(mode.equals(GENERAL_PICTURE_MODE)){
            intent.putStringArrayListExtra(TAKEN_PICTURES_KEY, takenPictures);
        }
        intent.putExtra(PICTURE_CAPTURE_MODE, mode);
        intent.putExtra(ITEM_ID_KEY, position);
        intent.putExtra(ITEM_KEY, item);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void putTimeStamp(){
        Bitmap src = BitmapFactory.decodeFile(futurePath);
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        String dateTime = SDF.format(Calendar.getInstance().getTime());
        String projectLabel, drawingLabel, partLabel;
        projectLabel = getResources().getString(R.string.projectLabel);
        drawingLabel = getResources().getString(R.string.drawingLabel);
        partLabel = getResources().getString(R.string.partLabel);
        String projectInfo = projectLabel
                .concat(": ").concat(project.getReference())
                .concat("\n").concat(drawingLabel).concat(": ").concat(String.valueOf(project.getDrawingRefs().get(0).getDnumber()))
                .concat("\n").concat(partLabel).concat(": ").concat(String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()))
                .concat("\n");
        String itemInfo;

        Canvas cs = new Canvas(dest);
        Paint tPaint = new Paint();
        tPaint.setTextSize(85);
        tPaint.setColor(Color.YELLOW);
        tPaint.setStyle(Paint.Style.FILL);
        float height = tPaint.measureText("yY");
        cs.drawBitmap(src, 0f, 0f, null);
        cs.drawText(projectInfo.concat(" - ").concat(dateTime),20f, height + 15f, tPaint);
        if(mode.equals(ITEM_PICTURE_MODE)) {
            itemInfo = report.toString().concat(", ").concat(getResources().getString(R.string.itemTag))
                    .concat(": ").concat(String.valueOf(item.getNumber()));

        } else {

            itemInfo = "QP" + (ProjectHelper.getCurrentPicNumber(project) - 1);
        }
        cs.drawText(itemInfo,20f, 2*height + 15f, tPaint);

        try {
            dest.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(futurePath)));
        } catch (FileNotFoundException e) {
            logger.warning(e.toString());
        }
    }
}
