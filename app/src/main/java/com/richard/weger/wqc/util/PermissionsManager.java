package com.richard.weger.wqc.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.richard.weger.wqc.appconstants.AppConstants.INTRINSIC_PERMISSIONS_CODE;

public class PermissionsManager extends ActivityCompat {

    public static boolean checkPermission(String[] permissions, Activity activity, Boolean askIfDontHave) {
        boolean needToAsk = false;
        for(String s:permissions){
            if((ContextCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED)){
                needToAsk = true;
                break;
            }
        }
        if (needToAsk) {
            if(!askIfDontHave) return false;
            else askPermission(permissions, activity);
            return false;
        } else {
            return true;
        }
    }

    public static void askPermission(String[] permissions, Activity activity){
        ActivityCompat.requestPermissions(activity, permissions, INTRINSIC_PERMISSIONS_CODE);
    }

}