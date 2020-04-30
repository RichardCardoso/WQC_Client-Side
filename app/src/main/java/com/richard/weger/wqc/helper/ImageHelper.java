package com.richard.weger.wqc.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.activity.CameraActivity;
import com.richard.weger.wqc.activity.PictureViewerActivity;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.util.GeneralPictureDTO;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.PICTURES_AUTHORITY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURES_LIST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_START_INDEX_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_VIEWER_SCREEN_ID;
import static com.richard.weger.wqc.appconstants.AppConstants.REQUEST_IMAGE_CAPTURE_ACTION;
import static com.richard.weger.wqc.appconstants.AppConstants.SDF;
import static com.richard.weger.wqc.util.App.getStringResource;

public class ImageHelper {

    // https://stackoverflow.com/questions/28424942/decrease-image-size-without-losing-its-quality-in-android
    public static String compressImage(String filePath) {

//        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

//        float maxHeight = 816.0f;
//        float maxWidth = 612.0f;
        float maxHeight = actualHeight;
        float maxWidth = actualWidth;
        float imgRatio = (float) (actualWidth / actualHeight);
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inTempStorage = new byte[16 * 1024];
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inScaled = false;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError e) {
            LoggerManager.getLogger(ImageHelper.class).warning(e.toString());
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            LoggerManager.getLogger(ImageHelper.class).warning(e.toString());
            return null;
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - (float) bmp.getWidth() / 2, middleY - (float) bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            LoggerManager.getLogger(ImageHelper.class).warning(e.toString());
        }

        FileOutputStream out;
        String compressedFilename = getFilename();
        try {
            out = new FileOutputStream(compressedFilename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        } catch (FileNotFoundException e) {
            LoggerManager.getLogger(ImageHelper.class).warning(e.toString());
        }

        String returnFilename;
        returnFilename = revertTempPicName(filePath, true);
        FileHelper.fileCopy(new File(compressedFilename), new File(returnFilename));
        FileHelper.fileDelete(filePath);
        FileHelper.fileDelete(compressedFilename);

        return returnFilename;

    }

    public static String getTempPicName(String name) {
        String ret;
        if(name.contains(".jpg")) {
            ret = name.replace(".jpg", "_new" + UUID.randomUUID().toString() + ".jpg");
        } else {
            ret = name;
        }
        return ret;
    }

    public static String revertTempPicName(String name, boolean appendExtension){
        String ret;
        if(name.contains("_new")) {
            ret = name.substring(0, name.indexOf("_new"));
        } else {
            ret = name;
        }
        if(appendExtension && !ret.endsWith(".jpg")) {
            ret = ret.concat(".jpg");
        }
        return ret;
    }

    public static String getFinalFilename(String temporaryFilepath) {
        if (temporaryFilepath == null) {
            return null;
        }
        String finalFilepath = revertTempPicName(temporaryFilepath, true);
        return finalFilepath.substring(finalFilepath.lastIndexOf(File.separator) + 1);
    }

    public static String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    @NonNull
    public static File createImageFile(String folderPath, String imageFileName) throws IllegalStateException {
        File image = null;
        try {
            File storageDir = new File(folderPath);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            imageFileName = folderPath.concat(imageFileName);

            try {
                image = new File(getTempPicName(imageFileName));
                if (image.exists()) {
                    image.delete();
                }
                image.createNewFile();
            } catch (Exception e) {
                try {
                    imageFileName = imageFileName.substring(
                            imageFileName.lastIndexOf('/') + 1,
                            imageFileName.lastIndexOf('.'));
                    if (imageFileName.length() - imageFileName.lastIndexOf('-') > 15)
                        imageFileName = imageFileName.substring(0,
                                imageFileName.lastIndexOf('-') + 9);
                    image = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                } catch (IOException e2) {
                    LoggerManager.getLogger(ImageHelper.class).severe(StringHelper.getStackTraceAsString(e2));
                }
            }
        } catch (Exception ex){
            LoggerManager.getLogger(ImageHelper.class).severe(StringHelper.getStackTraceAsString(ex));
            throw new IllegalStateException("Failed to create image file for a picture capture!");
        }

        if(image == null){
            throw new IllegalStateException("Failed to create image file for a picture capture!");
        }

        return image;
    }

    public static List<String> getExistingGeneralPictures(Project project){
        File picFolder = new File(StringHelper.getPicturesFolderPath(project));
        List<String> ret = new ArrayList<>();
        Arrays.stream(picFolder.listFiles()).filter(f -> f.getName().contains("QP")).forEach(p -> ret.add(p.getName()));
        return ret;
    }

    public static void takePicture(Activity delegate) {
        try {
            Intent takePictureIntent;
            takePictureIntent = new Intent(delegate, CameraActivity.class);
            if (takePictureIntent.resolveActivity(delegate.getPackageManager()) != null) {
                delegate.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_ACTION);
            } else {
                throw new IllegalStateException("No camera intent found on this device. Maybe it does not have this function.");
            }
        } catch (Exception ex) {
            ImageHelper.pictureCaptureSetupException(delegate, ex);
        }
    }

    public static String takePicture(Activity delegate, Project project, Item item) {
        File photoFile;
        try {
            photoFile = ImageHelper.createImageFile(StringHelper.getPicturesFolderPath(project), StringHelper.getItemImageFilename(item));
            return ImageHelper.takePicture(photoFile, delegate);
        } catch (Exception ex) {
            ImageHelper.pictureCaptureSetupException(delegate, ex);
            return null;
        }
    }

    private static String takePicture(@NonNull File photoFile, Activity delegate) throws IllegalStateException {
        Intent takePictureIntent;
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(delegate.getPackageManager()) != null) {
            Uri photoURI = FileProvider.getUriForFile(delegate,
                    PICTURES_AUTHORITY,
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            delegate.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_ACTION);
            return photoFile.getAbsolutePath();
        } else {
            throw new IllegalStateException("No camera intent found on this device. Maybe it does not have this function.");
        }
    }

    private static byte[] getFileAsBytes(String path){
        File f = new File(path);
        int size = (int) f.length();
        byte[] bytes = new byte[size];
        try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f))){
            buf.read(bytes, 0, bytes.length);
            return bytes;
        } catch (Exception ex) {
            return null;
        }
    }

    private static Bitmap getBitmapFromBytes(final byte[] bytes){
        if(bytes != null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inScaled = false;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } else {
            return null;
        }
    }

    private static Bitmap getBitmapFromBytes(String path) {
        byte[] bytes = getFileAsBytes(path);
        return getBitmapFromBytes(bytes);
    }

    public static void putPicInfoAndTimestamp(String picPath, Project project, String picInfo){
        Bitmap src = BitmapFactory.decodeFile(picPath);
        if(src != null) {
            if (src.getWidth() > src.getHeight()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(src, src.getWidth(), src.getHeight(), true);
                src = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            }
            Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
            String dateTime = SDF.format(Calendar.getInstance().getTime());
            String projectInfo = project.getReference()
                    .concat("\n").concat("Z").concat(String.valueOf(project.getDrawingRefs().get(0).getDnumber()))
                    .concat("\n").concat("T").concat(String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()))
                    .concat("\n");

            Canvas cs = new Canvas(dest);
            Paint tPaint = new Paint();
            tPaint.setTextSize((float) 40 / 1080 * src.getWidth());
            tPaint.setColor(Color.YELLOW);
            tPaint.setStyle(Paint.Style.FILL);
            float height = tPaint.measureText("yY");
            cs.drawBitmap(src, 0f, 0f, new Paint(Paint.ANTI_ALIAS_FLAG));

            Paint rPaint = new Paint();
            rPaint.setColor(Color.BLACK);
            tPaint.setStyle(Paint.Style.FILL);

            String t = projectInfo.concat(" - ").concat(dateTime);
            cs.drawRect(10f, 10f, 10f + tPaint.measureText(t) + 25f, 10f + height + 25f, rPaint);
            cs.drawText(t, 20f, height + 15f, tPaint);

            cs.drawRect(10f, 10f + height + 10f, 10f + tPaint.measureText(picInfo) + 25f, 10f + 2 * height + 25f, rPaint);
            cs.drawText(picInfo, 20f, 2 * height + 15f, tPaint);

            try {
                dest.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(picPath)));
            } catch (FileNotFoundException e) {
                LoggerManager.getLogger(ImageHelper.class).warning(e.toString());
            }
        }
    }

    public static String getPrefixedPicNumber(Project project) {
        return "QP" + (ProjectHelper.getCurrentPicNumber(project) - 1);
    }

    public static String getPrefixedPicNumber(Item item){
        return "Q ".concat(String.valueOf(item.getNumber()));
    }

    private static void pictureCaptureSetupException(Context context, Exception ex){
        String message = getStringResource(R.string.photoCaptureError).concat("\n(").concat(getStringResource(R.string.tryAgainLaterMessage));
        LoggerManager.getLogger(PictureViewerActivity.class).severe(StringHelper.getStackTraceAsString(ex));
        ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.CLIENT_PHOTO_CAPTURE_SETTING_EXCEPTION, message, ErrorResult.ErrorLevel.SEVERE);
        ErrorResponseHandler.handle(err, null);
    }

    public static void showPicture(List<GeneralPictureDTO> picsList, int position, Activity delegate){
        if(delegate instanceof RestTemplateHelper.RestResponseHandler) {
            RestTemplateHelper.RestResponseHandler h = (RestTemplateHelper.RestResponseHandler) delegate;
            h.toggleControls(false);
        }
        Intent intent = new Intent(delegate, PictureViewerActivity.class);
        intent.putStringArrayListExtra(PICTURES_LIST_KEY, (ArrayList<String>) picsList.stream().map(GeneralPictureDTO::getFileName).collect(Collectors.toList()));
        intent.putExtra(PICTURE_START_INDEX_KEY, position);
        delegate.startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
    }


    public static float getImageRotation(String filePath){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        if(options.outWidth > options.outHeight) {
            return 90f;
        } else {
            return 0f;
        }
    }

    public static Bitmap getScaledAndRotatedBitmap(String filePath, boolean highRes){
        Bitmap original, scaled;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        if(highRes) {
            options.inSampleSize = calculateInSampleSize(options, options.outWidth, options.outHeight);
        } else {
            options.inSampleSize = calculateInSampleSize(options, 30, 30);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        original = BitmapFactory.decodeFile(filePath, options);

        if(original == null) {
            return null;
        }

        if(original.getWidth() > original.getHeight()) {
            Matrix matrix;
            matrix = new Matrix();
            matrix.postRotate(90);
            scaled = Bitmap.createScaledBitmap(original, original.getWidth(), original.getHeight(), true);
            original = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
        }
        return original;
    }

    public static Bitmap getScaledAndRotatedThumbnail(String filePath){
        return ThumbnailUtils.extractThumbnail(getScaledAndRotatedBitmap(filePath, false),150, 150);
    }

    public static class RotateTransformation extends BitmapTransformation {

        private float rotateRotationAngle = 0f;

        public RotateTransformation(Context context, float rotateRotationAngle) {
            super( context );

            this.rotateRotationAngle = rotateRotationAngle;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();

            matrix.postRotate(rotateRotationAngle);

            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
        }

        @Override
        public String getId() {
            return "rotate" + rotateRotationAngle;
        }
    }


}
