package com.richard.weger.wqc.service;

import android.app.Activity;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.MessageboxHelper;
import com.richard.weger.wqc.rest.RestResult;

import org.springframework.http.HttpStatus;

import static com.richard.weger.wqc.helper.LogHelper.writeData;

public abstract class ErrorResponseHandler {
    public static void handle(RestResult result, Activity delegate, MessageboxHelper.Method method){
        HttpStatus status = result.getStatus();
        if(result != null && result.getMessage() != null){
            writeData(result.getMessage());
        } else {
            writeData("Unknown error");
        }
        if(status == HttpStatus.CONFLICT) {
            MessageboxHelper.showMessage(delegate,
                    delegate.getResources().getString(R.string.staleDataMessage),
                    delegate.getResources().getString(R.string.okTag),
                    method);
        } else {
            MessageboxHelper.showMessage(delegate,
                    delegate.getResources().getString(R.string.dataRecoverError),
                    delegate.getResources().getString(R.string.okTag),
                    method);
        }
    }
}
