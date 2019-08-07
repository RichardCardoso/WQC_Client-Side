package com.richard.weger.wqc.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.richard.weger.wqc.R;

public class MessageboxHelper {
    public interface Method {
        void execute();
    }

    public interface InputBoxContentHandler {
        void processInputboxResult(String content);
    }

    public static void getString(Activity delegate, String message, InputBoxContentHandler handler){
        LayoutInflater li = LayoutInflater.from(delegate);
        View view = li.inflate(R.layout.input_box, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(delegate);

        builder.setView(view);

        final EditText txtContent = view.findViewById(R.id.editContent);
        final TextView tvMessage = view.findViewById(R.id.tvMessage);

        tvMessage.setText(message);
        builder
                .setCancelable(false)
                .setPositiveButton(delegate.getResources().getString(R.string.okTag), (dialog, which) -> handler.processInputboxResult(txtContent.getText().toString()))
                .setNegativeButton(delegate.getResources().getString(R.string.cancelTag), ((dialog, which) -> handler.processInputboxResult(null)));

        try {
            builder.create().show();
        } catch (Exception ignored){

        }
    }

    public static void showMessage(Activity delegate, String title, String message, String positiveTag, String negativeTag, Method positiveCallback, Method negativeCallback){
        AlertDialog.Builder builder = new AlertDialog.Builder(delegate);
        builder.setCancelable(false);
        if(title != null){
            builder.setTitle(title);
        }
        if(message != null){
            builder.setMessage(message);
        }
        if(positiveTag != null){
            if (positiveCallback != null) {
                builder.setPositiveButton(positiveTag, (dialog, which) -> positiveCallback.execute());
            } else {
                builder.setPositiveButton(positiveTag, (dialog, which) -> {});
            }
        }
        if(negativeTag != null){
            if(negativeCallback != null) {
                builder.setNegativeButton(negativeTag, (dialog, which) -> negativeCallback.execute());
            } else {
                builder.setNegativeButton(negativeTag, (dialog, which) -> {});
            }
        }
        try {
            builder.show();
        } catch (Exception ignored){}
    }

    public static void showMessage(Activity delegate, String message, String positiveTag, Method method){
        AlertDialog.Builder builder = new AlertDialog.Builder(delegate);
        builder.setCancelable(false);
        if(message != null){
            builder.setMessage(message);
        }
        if(positiveTag != null){
            if (method != null) {
                builder.setPositiveButton(positiveTag, (dialog, which) -> method.execute());
            } else {
                builder.setPositiveButton(positiveTag, (dialog, which) -> {});
            }
        }
        try {
            builder.show();
        } catch (Exception ignored){}
    }

}
