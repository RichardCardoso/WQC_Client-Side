package com.richard.weger.wegerqualitycontrol.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.DATA_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.REQUEST_IMAGE_CAPTURE_ACTION;

public class CameraHandler{
    public static void dispatchTakePictureIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            ((Activity)context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_ACTION);
        }
    }

    public static Bitmap handleTakePictureIntentResponse(int requestCode, int resultCode, Intent data){
        Bitmap bitmap = null;
        if (requestCode == REQUEST_IMAGE_CAPTURE_ACTION && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) (extras != null ? extras.get(DATA_KEY) : null);
        }
        return bitmap;
    }
}
