package com.richard.weger.wqc.exception;

import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.util.App;

import java.io.IOException;

public class DataRecoverException extends IOException {
    public DataRecoverException(String message){
        super(message);
//        Toast.makeText(App.getContext(), App.getContext().getResources().getString(R.string.dataRecoverError), Toast.LENGTH_LONG).show();
    }
}
