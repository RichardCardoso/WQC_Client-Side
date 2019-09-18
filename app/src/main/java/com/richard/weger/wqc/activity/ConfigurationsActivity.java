package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.LoggerManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class ConfigurationsActivity extends Activity {

    private static Logger logger = LoggerManager.getLogger(ConfigurationsManager.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_configurations);

        fillFields();
        setListeners();
    }

    private void fillFields(){
        TextView textView;
        Configurations conf = ConfigurationsManager.getLocalConfig();

        textView = findViewById(R.id.editServerPath);
        textView.setText(conf.getServerPath());

        logger.info("Finished filling configurations activity fields");
    }

    private void setListeners(){
        Button btn;

        logger.info("Started setting configurations activity listeners");
        btn = findViewById(R.id.btnSave);
        btn.setOnClickListener(view -> saveConfig());

        btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        logger.info("Finished setting configurations activity listeners");
    }

    private void saveConfig(){
        Configurations conf = new Configurations();
        TextView textView;
        int i = 0;

        logger.info("Retrieving configurations activity fields values");

        textView = findViewById(R.id.editServerPath);
        conf.setServerPath(textView.getText().toString());

        logger.info("Validating configurations activity fields values");
        for(Field f:conf.getClass().getDeclaredFields()){
            f.setAccessible(true);
            String fieldValue = (String)runGetter(f, new Configurations());
            if(fieldValue != null) {
                if (fieldValue.equals("")) {
                    i++;
                }
            } else {
                if(f.getClass().isAssignableFrom(String.class)) {
                    Toast.makeText(this, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        logger.info("Finished validating configurations activity fields values");

        if(i == 0) {
            logger.info("Requesting project's json export");
            ConfigurationsManager.setLocalConfig(conf);
            Toast.makeText(this, R.string.valuesSavedMessage, Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, R.string.emptyFieldsError, Toast.LENGTH_LONG).show();
            return;
        }

        setResult(RESULT_OK);
        finish();
    }

    public static Object runGetter(Field field, Configurations o)
    {
        // MZ: Find the correct Method
        for (Method method : o.getClass().getMethods())
        {
            if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3)))
            {
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase()))
                {
                    // MZ: Method found, export it
                    try
                    {
                        return method.invoke(o);
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        logger.warning(e.toString());
                    }

                }
            }
        }


        return null;
    }
}
