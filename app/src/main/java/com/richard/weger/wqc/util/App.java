package com.richard.weger.wqc.util;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.StringHelper;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class App extends Application implements MyActivityLifecycleCallback.ActivityLifecycleCallbackHandler {

    private static Activity currentActivity;

    private static App context;

    private static SharedPreferences mPrefs;

    private static Set<String> processedMessages = new HashSet<>();

    @Override
    public void onCreate(){
        super.onCreate();
        context = this;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallback(this));
        try {
            createNotificationChannel();
        } catch (Exception ex) {
            String sEx = StringHelper.getStackTraceAsString(ex);
            LoggerManager.getLogger(App.class).warning("Failed to create a notification channel!\n" + sEx);
        }
    }

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

    public static String getStringResource(@StringRes int id) {
        return getContext().getString(id);
    }

    public static Drawable getDrawableResource(@DrawableRes int id) {
        return ContextCompat.getDrawable(getContext(), id);
    }

    public static Context getContext(){
        return context;
    }

    public static String getUniqueId(){
        return Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    public static synchronized Activity getCurrentActivity() {
        return currentActivity;
    }

    @Override
    public synchronized void activityResumed(Activity activity) {
        App.currentActivity = activity;
    }

    public static void createNotificationChannel(){
        CharSequence name = getStringResource(R.string.updatesChannelName);
        String description = getStringResource(R.string.updatesChannelDescription);
        String id = getStringResource(R.string.updatesChannelId);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager manager = App.getContext().getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

}
