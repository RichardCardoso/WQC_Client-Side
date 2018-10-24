package com.richard.weger.wqc.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import java.io.File;

import static com.richard.weger.wqc.util.LogHandler.writeData;

public class ConfigurationsManager{

    private static File extFilesDir = App.getContext().getExternalFilesDir(null);
    private static SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    private static ParamConfigurations pConfigs;

    public static void setServerConfig(ParamConfigurations pConfigs){
        ConfigurationsManager.pConfigs = pConfigs;
    }

    public static ParamConfigurations getServerConfig(){
        return pConfigs;
    }

    public static void setLocalConfig(Configurations config){
        writeData("Starting project's json export", extFilesDir);
        Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(config);
        prefsEditor.putString(Configurations.class.getName(), json);
        prefsEditor.apply();
        writeData("Finished project's json export", App.getContext().getExternalFilesDir(null));
    }

    public static Configurations getLocalConfig(){
        writeData("Starting project's json load", App.getContext().getExternalFilesDir(null));
        Gson gson = new Gson();
        String json = mPrefs.getString(Configurations.class.getName(), "");
        if(json.equals("")){
            writeData("Project's json file not found. The app will create and use a file with the default configurations.", extFilesDir);
            setLocalConfig(new Configurations());
            return new Configurations();
        }
        writeData("Finished project's json load", App.getContext().getExternalFilesDir(null));
        return gson.fromJson(json, Configurations.class);
    }


}
