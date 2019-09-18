package com.richard.weger.wqc.helper;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.common.util.Strings;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.util.App;

import java.util.ArrayList;
import java.util.List;

public class AlertHelper {

    private static List<Integer> notificationIds = new ArrayList<>();

    public interface Method {
        void execute();
    }

    public interface InputBoxContentHandler {
        void processInputboxResult(String content);
    }

    public static void getString(Context delegate, String message, InputBoxContentHandler handler){
        getString(delegate, message, handler, null);
    }

    public static void getString(Context delegate, String message, InputBoxContentHandler handler, String defaultText){
        LayoutInflater li = LayoutInflater.from(delegate);
        View view = li.inflate(R.layout.input_box, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(delegate);

        builder.setView(view);

        final EditText txtContent = view.findViewById(R.id.editContent);
        final TextView tvMessage = view.findViewById(R.id.tvMessage);

        if(!Strings.isEmptyOrWhitespace(defaultText)){
            txtContent.setText(defaultText);
        }

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

    public static void showMessage(Context delegate, String title, String message, String positiveTag, String negativeTag, Method positiveCallback, Method negativeCallback){
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

    public static void showMessage(Context delegate, String message, String positiveTag, Method method){
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

    public static void showNotification(String title, String message){
        Context context = App.getContext();
        String channelId = context.getResources().getString(R.string.updatesChannelId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTimeoutAfter(10000)
                .setAutoCancel(true);
        int notificationId = notificationIds.size() + 1;
        notificationIds.add(notificationId);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancelAll();
        manager.notify(notificationId, builder.build());
    }

}
