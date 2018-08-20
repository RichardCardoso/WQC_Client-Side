package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.richard.weger.wegerqualitycontrol.domain.Configurations;

import java.io.Serializable;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public abstract class ConfigurationsManager implements Serializable{


    public static void saveConfig(Configurations config, Context context){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(config);
        prefsEditor.putString(Configurations.class.getName(), json);
        prefsEditor.apply();
    }

    public static Configurations loadConfig(Context context){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = mPrefs.getString(Configurations.class.getName(), "");
        if(json.equals("")){
            return null;
        }
        return gson.fromJson(json, Configurations.class);
    }

}
