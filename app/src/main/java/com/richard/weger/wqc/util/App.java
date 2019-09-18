package com.richard.weger.wqc.util;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.richard.weger.wqc.R;

public class App extends Application {
    private static App context;

    private static SharedPreferences mPrefs;

    public static String getExpectedVersion() {
        return "2.7.0.0";
    }

    public static SharedPreferences getmPrefs() {
        return mPrefs;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        context = this;
        createNotificationChannel();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Context getContext(){
        return context;
    }

    public static String getUniqueId(){
        return Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void createNotificationChannel(){
        CharSequence name = App.getContext().getResources().getString(R.string.updatesChannelName);
        String description = App.getContext().getResources().getString(R.string.updatesChannelDescription);
        String id = App.getContext().getResources().getString(R.string.updatesChannelId);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager manager = App.getContext().getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }
}
