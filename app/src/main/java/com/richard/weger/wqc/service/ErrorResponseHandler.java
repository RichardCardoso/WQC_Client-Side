package com.richard.weger.wqc.service;

import com.google.android.gms.common.util.Strings;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.util.IMethod;
import com.richard.weger.wqc.util.LoggerManager;

import static com.richard.weger.wqc.util.App.getCurrentActivity;
import static com.richard.weger.wqc.util.App.getStringResource;

public abstract class ErrorResponseHandler {

    private static String handle(ErrorResult result){
        String message;
        if (!Strings.isEmptyOrWhitespace(result.getDescription())){
            message = result.getDescription();
        } else {
            message = getStringResource(R.string.unknownErrorMessage);
        }
        message += "\n\nError code: " + result.getCode();
        message += "\nError level: " + result.getLevel();
        LoggerManager.log(ErrorResponseHandler.class, result);
        return message;
    }

    public static void handle(ErrorResult result, IMethod IMethod){
        String message;
        message = handle(result);
        LoggerManager.log(ErrorResponseHandler.class, result);
        AlertHelper.showMessage(message, getStringResource(R.string.okTag), IMethod);
    }

    public static void handle(ErrorResult result, String positiveTag, String negativeTag, IMethod positiveIMethod, IMethod negativeIMethod){
        String message;
        message = handle(result);
        LoggerManager.log(ErrorResponseHandler.class, result);
        AlertHelper.showMessage(null, message, positiveTag, negativeTag, positiveIMethod, negativeIMethod, getCurrentActivity());
    }

}
