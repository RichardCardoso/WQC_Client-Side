package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.richard.weger.wegerqualitycontrol.domain.Configurations;

import java.io.Serializable;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;
import static com.richard.weger.wegerqualitycontrol.util.LogHandler.writeData;

public abstract class ConfigurationsManager implements Serializable{


    public static void saveConfig(Configurations config, Context context){
        writeData("Starting project's json export", context.getExternalFilesDir(null));
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(config);
        prefsEditor.putString(Configurations.class.getName(), json);
        prefsEditor.apply();
        writeData("Finished project's json export", context.getExternalFilesDir(null));
    }

    public static Configurations loadConfig(Context context){
        writeData("Starting project's json load", context.getExternalFilesDir(null));
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = mPrefs.getString(Configurations.class.getName(), "");
        if(json.equals("")){
            writeData("Project's json file not found. The app will create and use a file with the default configurations.", context.getExternalFilesDir(null));
            saveConfig(new Configurations(), context);
            return new Configurations();
        }
        writeData("Finished project's json load", context.getExternalFilesDir(null));
        return gson.fromJson(json, Configurations.class);
    }

}
