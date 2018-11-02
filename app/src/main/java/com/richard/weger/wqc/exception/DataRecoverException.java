package com.richard.weger.wqc.exception;

import java.io.IOException;

public class DataRecoverException extends IOException {
    public int id = 0;
    public DataRecoverException(String message){
        super(message);
//        Toast.makeText(App.getContext(), App.getContext().getResources().getString(R.string.dataRecoverError), Toast.LENGTH_LONG).show();
    }

    public DataRecoverException(String message, int id){
        super(message);
        this.id = id;
//        Toast.makeText(App.getContext(), App.getContext().getResources().getString(R.string.dataRecoverError), Toast.LENGTH_LONG).show();
    }

}
