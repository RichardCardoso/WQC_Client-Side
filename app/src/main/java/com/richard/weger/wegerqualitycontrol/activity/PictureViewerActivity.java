package com.richard.weger.wegerqualitycontrol.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Item;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.util.CameraHandler;
import com.richard.weger.wegerqualitycontrol.util.ItemAdapter;
import com.richard.weger.wegerqualitycontrol.util.ProxyBitmap;
import com.richard.weger.wegerqualitycontrol.util.StringHandler;

import java.io.File;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.ITEM_ID_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.ITEM_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PICTURES_AUTHORITY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PICTURE_VIEWER_SCREEN_ID;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PROJECT_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.REQUEST_IMAGE_CAPTURE_ACTION;

public class PictureViewerActivity extends Activity{

    Item item = new Item();
    int position = -1;
    PhotoViewAttacher mAttacher;
    ImageView imageView;
    String futurePath = "";
    Project project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);

        imageView = findViewById(R.id.ivItem);

        Intent intent = getIntent();
        if(intent != null){
            item = (Item) intent.getSerializableExtra(ITEM_KEY);
            position = intent.getIntExtra(ITEM_ID_KEY, -1);
            project = (Project) intent.getSerializableExtra(PROJECT_KEY);
            if(position == -1){
                Toast.makeText(this, R.string.dataRecoverError, Toast.LENGTH_LONG).show();
                resultCanceled();
            }
        }
        if(item != null){
            if (item.getPicture() != null){
                /*Bitmap bitmap = item.getPicture().getProxyBitmap().getBitmap();
                if(bitmap != null) {
                    ((ImageView) findViewById(R.id.ivItem)).setImageBitmap(bitmap);
                }*/
                String picPath = item.getPicture().getFilePath();
                File file = new File(picPath);
                if(file.exists()){
                    //setPic();
                    imageView.setImageURI(Uri.parse(picPath));
                }
            }
        }

        findViewById(R.id.btnTakeNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultCanceled();
            }
        });

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
                Toast.makeText(this, "Error when trying to create the image file",
                        Toast.LENGTH_LONG).show();
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

    private File createImageFile(){
        // Create an image file name
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = item.getPicture().getFilePath();
        String folderPath = StringHandler.generateProjectFolderName(
                getExternalFilesDir(null), project).concat("Pictures/");
        File storageDir = new File(folderPath); //getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(!storageDir.exists()){
            storageDir.mkdir();
        }

        File image;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
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
                e2.printStackTrace();
                return null;
            }
        }
        // Save a file: path for use with ACTION_VIEW intents
        futurePath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        ImageView mImageView = findViewById(R.id.ivItem);
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(item.getPicture().getFilePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(item.getPicture().getFilePath(), bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Bitmap bitmap = CameraHandler.handleTakePictureIntentResponse(requestCode, resultCode, data);
        Intent intent = new Intent();
        // item.getPicture().setProxyBitmap(new ProxyBitmap(bitmap));
        if(resultCode == RESULT_OK)
            item.getPicture().setFilePath(futurePath);
        intent.putExtra(ITEM_ID_KEY, position);
        intent.putExtra(ITEM_KEY, item);
        setResult(RESULT_OK, intent);
        finish();
    }
}
