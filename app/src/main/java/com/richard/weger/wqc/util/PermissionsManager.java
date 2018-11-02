package com.richard.weger.wqc.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static com.richard.weger.wqc.constants.AppConstants.INTRINSIC_PERMISSIONS_CODE;

public class PermissionsManager extends ActivityCompat {

    public boolean checkPermission(String[] permissions, Activity activity, Boolean askIfDontHave) {
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

    public void askPermission(String[] permissions, Activity activity){
        ActivityCompat.requestPermissions(activity, permissions, INTRINSIC_PERMISSIONS_CODE);
    }

}