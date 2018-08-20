package com.richard.weger.wegerqualitycontrol.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.CAMERA_PERMISSION_CODE;

public class PermissionsManager extends ActivityCompat {

    public boolean checkPermission(String permission, Activity activity, Boolean askIfDontHave) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if(!askIfDontHave) return false;
            else askPermission(permission, activity);
        }
        return true;
    }

    public void askPermission(String permission, Activity activity){
        String[] permissions = new String[]{permission};
        ActivityCompat.requestPermissions(activity, permissions, CAMERA_PERMISSION_CODE);
    }

}