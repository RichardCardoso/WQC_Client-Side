package com.richard.weger.wqc.util;

import android.app.Application;
import android.content.Context;
import android.provider.Settings;

public class App extends Application {
    private static App context;

    public static String getExpectedVersion() {
        return "2.6.0.2";
    }

    @Override
    public void onCreate(){
        super.onCreate();
        context = this;
    }

    public static Context getContext(){
        return context;
    }

    public static String getUniqueId(){
        return Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
