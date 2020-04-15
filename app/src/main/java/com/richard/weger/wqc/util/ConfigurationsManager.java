package com.richard.weger.wqc.util;

import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.helper.ActivityHelper;
import com.richard.weger.wqc.helper.QrTextHelper;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.service.ParamConfigurationsRequestParametersResolver;

import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.REST_CONFIGLOAD_KEY;

public class ConfigurationsManager{

    private static boolean firstTimeGet = true;
    private static boolean firstTimeSet = true;

    private static ParamConfigurations pConfigs;

    private static Logger logger = LoggerManager.getLogger(ConfigurationsManager.class);

    public static void setServerConfig(ParamConfigurations pConfigs){
        ConfigurationsManager.pConfigs = pConfigs;
    }

    public static ParamConfigurations getServerConfig(){
        return pConfigs;
    }

    public static void loadServerConfig(RestTemplateHelper.RestResponseHandler handler){
        logger.info("Started routine to get the configs from server");
        Resources r = App.getContext().getResources();
        String message = String.format(r.getConfiguration().getLocales().get(0), "%s, %s",
                r.getString(R.string.configLoadingMessage),
                r.getString(R.string.pleaseWaitMessage).toLowerCase());
        ActivityHelper.setHandlerWaitingLayout(handler, message);
        ParamConfigurationsRequestParametersResolver resolver = new ParamConfigurationsRequestParametersResolver(REST_CONFIGLOAD_KEY, null, true);
        resolver.getEntity(new ParamConfigurations(), handler);
    }

    public static void setLocalConfig(Configurations config){
        if(firstTimeSet) {
            logger.info("Starting configs json export");
        }
        Gson gson = new Gson();
        String json = gson.toJson(config);
        Editor prefsEditor = App.getmPrefs().edit();
        LoggerManager.getLogger(QrTextHelper.class).severe("Configs json:\n" + json);
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
        String json = App.getmPrefs().getString(Configurations.class.getName(), "");

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
