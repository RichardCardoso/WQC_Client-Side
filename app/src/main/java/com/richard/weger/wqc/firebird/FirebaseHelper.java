package com.richard.weger.wqc.firebird;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.ActivityHelper;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class FirebaseHelper extends FirebaseMessagingService {

    public static FirebaseListener delegate;

    public interface FirebaseListener{
        void messageReceived(String qrCode, Long entityId);
        void onConnectionSuccess();
    }

    public static void firebaseConfig(FirebaseHelper.FirebaseListener listener){
        if (listener instanceof Activity){
            Activity t;
            Resources r;
            String message;

            t = (Activity) listener;
            r = App.getContext().getResources();
            message = String.format(r.getConfiguration().getLocales().get(0), "%s, %s",
                    r.getString(R.string.firebaseConnectingMessage), r.getString(R.string.pleaseWaitMessage).toLowerCase());

            ActivityHelper.setWaitingLayout(t, message);
        }
        FirebaseHelper.delegate = listener;
        FirebaseHelper.subscribe();
    }

    public static void subscribe(){
        unsubscribe();
        FirebaseMessaging.getInstance().subscribeToTopic("WQC2-0").addOnSuccessListener(aVoid -> delegate.onConnectionSuccess());
    }

    public static void unsubscribe(){
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
        Log.d("PushMessage", "From: " + remoteMessage.getFrom());

        if(remoteMessage.getData().size() > 0){
            Log.d("PushMessage", "Message data payload: " + remoteMessage.getData());
            if(delegate != null) {
                Map<String, String> data = remoteMessage.getData();
                String qrCode = data.get("qrCode");
                String sId = data.get("id");
                if(qrCode != null && qrCode.equals(ProjectHelper.getQrCode())){
                    Resources r;
                    r = App.getContext().getResources();

                    AlertHelper.showNotification(r.getString(R.string.projectUpdatedMessage), qrCode);

                    try{
                        Long id = Long.valueOf(sId);
                        delegate.messageReceived(qrCode, id);
                    } catch (Exception ex){
                        LoggerManager.log(getClass(), "Error during push message id parse!\n" + ex.getMessage(), ErrorResult.ErrorLevel.SEVERE);
                    }

                }
            }
        }
    }
}
