package com.richard.weger.wqc.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.richard.weger.wqc.domain.ParamConfigurations;

import java.util.logging.Logger;

public class ConfigurationsManager{

    private static boolean firstTimeGet = true;
    private static boolean firstTimeSet = true;

    private static SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    private static ParamConfigurations pConfigs;

    private static Logger logger = LoggerManager.getLogger(ConfigurationsManager.class);

    public static void setServerConfig(ParamConfigurations pConfigs){
        ConfigurationsManager.pConfigs = pConfigs;
    }

    public static ParamConfigurations getServerConfig(){
        return pConfigs;
    }

    public static void setLocalConfig(Configurations config){
        if(firstTimeSet) {
            logger.info("Starting configs json export");
        }
        Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(config);
        prefsEditor.putString(Configurations.class.getName(), json);
        prefsEditor.apply();
        if(firstTimeSet){
            logger.info("Finished configs json export");
            firstTimeSet = false;
        }
    }

    public static Configurations getLocalConfig(){
        if(firstTimeGet) {
            logger.info("Starting configurations json load");
        }
        Gson gson = new Gson();
        String json = mPrefs.getString(Configurations.class.getName(), "");

        if(json == null || json.isEmpty()){
            logger.warning("Configs json file not found. The app will create and use a file with the default configurations.");
            Configurations conf = new Configurations();
            setLocalConfig(conf);
            return conf;
        }
        Configurations conf = gson.fromJson(json, Configurations.class);
        if(firstTimeGet) {
            logger.info("Finished configs  json load");
            firstTimeGet = false;
        }
        return conf;
    }

}
