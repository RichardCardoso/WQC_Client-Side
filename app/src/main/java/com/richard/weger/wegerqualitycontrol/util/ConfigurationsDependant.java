package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;

public abstract class ConfigurationsDependant {
    Configurations conf;

    public ConfigurationsDependant(Context context){
        conf = ConfigurationsManager.loadConfig(context);
        if(conf == null){
            conf = new Configurations();
            ConfigurationsManager.saveConfig(conf, context);
        }
    }
}
