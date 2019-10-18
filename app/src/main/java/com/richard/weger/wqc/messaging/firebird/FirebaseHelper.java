package com.richard.weger.wqc.messaging.firebird;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.richard.weger.wqc.helper.ActivityHelper;
import com.richard.weger.wqc.messaging.IMessagingListener;
import com.richard.weger.wqc.messaging.IMessagingService;
import com.richard.weger.wqc.messaging.MessagingHelper;
import com.richard.weger.wqc.messaging.websocket.MessagingDTO;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.IOException;
import java.util.Map;

public class FirebaseHelper extends FirebaseMessagingService implements IMessagingService {

    public static IMessagingListener delegate;

    public void setListener(IMessagingListener listener, boolean callbackToDelegate){
        LoggerManager.getLogger(FirebaseHelper.class).info("Setting firebird listener");
        FirebaseHelper.delegate = listener;
        subscribe();
    }

    public void removeListener(){
        LoggerManager.getLogger(FirebaseHelper.class).info("Removing firebird listener");
        delegate = null;
    }

    private void setup(IMessagingListener listener){
        ActivityHelper.activityDisable(listener);
        setListener(listener, false);
        subscribe();
    }


    private static void subscribe(){
        LoggerManager.getLogger(FirebaseHelper.class).info("Subscribing to firebird WQC2-0 topic");
        AsyncTask.execute(FirebaseHelper::unsubscribe);
        AsyncTask.execute(() -> {
            try {
                FirebaseMessaging instance;
                instance = FirebaseMessaging.getInstance();
                instance.subscribeToTopic("WQC2-0")
                        .addOnSuccessListener(aVoid -> delegate.onConnectionSuccess())
                        .addOnFailureListener((ex) -> MessagingHelper.failureHandler(ex, true));
            } catch (Exception e){
                MessagingHelper.failureHandler(e, true);
            }
        });
    }

    private static void unsubscribe(){
        LoggerManager.getLogger(FirebaseHelper.class).info("'Unsubscribe to Firebase WQC2-0 topic' routine has begun");
        try {
            FirebaseInstanceId instance;
            instance = FirebaseInstanceId.getInstance();
            instance.deleteInstanceId();
        } catch (IOException e) {
            MessagingHelper.failureHandler(e, false);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
        if(remoteMessage.getData().size() > 0){
            LoggerManager.getLogger(FirebaseHelper.class).info( "Message data payload: " + remoteMessage.getData());
            if(delegate != null) {
                Map<String, String> data = remoteMessage.getData();
                String qrCode = data.get("qrCode");
                String sId = data.get("id");
                String sParentId = data.get("parentId");
                MessagingDTO dto = new MessagingDTO();
                dto.setQrcode(qrCode);
                if (sId != null) {
                    dto.setId(Long.valueOf(sId));
                }
                if (sParentId != null) {
                    dto.setParentId(Long.valueOf(sParentId));
                }
                MessagingHelper.handleData(dto, delegate);
            }
        } else {
            LoggerManager.getLogger(FirebaseHelper.class).info( "Empty message payload was received!");
        }
    }
}
