package com.richard.weger.wqc.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import static com.richard.weger.wqc.helper.LogHelper.writeData;

public class ConfigurationsManager{

    private static SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    private static ParamConfigurations pConfigs;

    public static void setServerConfig(ParamConfigurations pConfigs){
        ConfigurationsManager.pConfigs = pConfigs;
    }

    public static ParamConfigurations getServerConfig(){
        return pConfigs;
    }

    public static void setLocalConfig(Configurations config){
        writeData("Starting configurations's json export");
        Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(config);
        prefsEditor.putString(Configurations.class.getName(), json);
        prefsEditor.apply();
        writeData("Finished configurations's json export");
    }

    public static Configurations getLocalConfig(){
        writeData("Starting configurations json load");
        Gson gson = new Gson();
        String json = mPrefs.getString(Configurations.class.getName(), "");
        if(json.equals("")){
            writeData("Project's json file not found. The app will create and use a file with the default configurations.");
            setLocalConfig(new Configurations());
            return new Configurations();
        }
        writeData("Finished configurations json load");
        return gson.fromJson(json, Configurations.class);
    }


}
