package com.richard.weger.wqc.firebird;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebirdMessagingService extends FirebaseMessagingService {

    public static FirebaseListener delegate;

    public interface FirebaseListener{
        public void messageReceived(Map<String, String> data);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Log.d("PushMessage", "From: " + remoteMessage.getFrom());

        if(remoteMessage.getData().size() > 0){
            Log.d("PushMessage", "Message data payload: " + remoteMessage.getData());
            if(delegate != null) {
                delegate.messageReceived(remoteMessage.getData());
            }
        }
    }
}
