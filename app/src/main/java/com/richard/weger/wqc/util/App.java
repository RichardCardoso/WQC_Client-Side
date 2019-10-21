package com.richard.weger.wqc.util;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.richard.weger.wqc.R;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class App extends Application {
    private static App context;

    private static SharedPreferences mPrefs;

    private static Set<String> processedMessages = new HashSet<>();

    public static boolean isValidVersion(String serverVersion) {
        if(serverVersion == null){
            return true;
        }

        String majorS, majorA;
        majorS = serverVersion.substring(0, serverVersion.lastIndexOf("."));
        majorA = getExpectedVersion().substring(0, getExpectedVersion().lastIndexOf("."));
        return (majorS.equals(majorA));

    }

    public static void markAsProcessed(String messageId){
        processedMessages.add(messageId);
    }

    public static boolean wasProcessed(String messageId) {
        return processedMessages.contains(messageId);
    }

    public static Locale getLocale(){
        return getContext().getResources().getConfiguration().getLocales().get(0);
    }

    public static String getExpectedVersion() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LoggerManager.getLogger(App.class).severe(e.getMessage());
            return "-1";
        }
    }

    public static SharedPreferences getmPrefs() {
        return mPrefs;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        context = this;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Context getContext(){
        return context;
    }

    public static String getUniqueId(){
        return Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void createNotificationChannel(){
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
