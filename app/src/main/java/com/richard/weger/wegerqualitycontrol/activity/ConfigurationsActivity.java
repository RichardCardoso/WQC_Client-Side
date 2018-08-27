package com.richard.weger.wegerqualitycontrol.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Configurations;
import com.richard.weger.wegerqualitycontrol.util.ConfigurationsManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class ConfigurationsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurations);

        fillFields();
        setListeners();
    }

    private void fillFields(){
        TextView textView;
        Configurations conf = ConfigurationsManager.loadConfig(this);

        textView = findViewById(R.id.editConstructionDrawingPath);
        textView.setText(conf.getConstructionDrawingPath());

        textView = findViewById(R.id.editTechnicDatasheetPath);
        textView.setText(conf.getTechnicDatasheetPath());

        textView = findViewById(R.id.editDrawingCode);
        textView.setText(conf.getDrawingCode());

        textView = findViewById(R.id.editDatasheetCode);
        textView.setText(conf.getDatasheetCode());

        textView = findViewById(R.id.editDrawingExtension);
        textView.setText(conf.getDrawingExtension());

        textView = findViewById(R.id.editDatasheetExtension);
        textView.setText(conf.getDatasheetExtension());

        textView = findViewById(R.id.editRootPath);
        textView.setText(conf.getRootPath());

        textView = findViewById(R.id.editServerPath);
        textView.setText(conf.getServerPath());

        textView = findViewById(R.id.editServerUsername);
        textView.setText(conf.getServerUsername());

        textView = findViewById(R.id.editServerPassword);
        textView.setText(conf.getServerPassword());

        textView = findViewById(R.id.editYearPrefix);
        textView.setText(conf.getYearPrefix());

        textView = findViewById(R.id.editAppPassword);
        textView.setText(conf.getAppPassword());
    }

    private void setListeners(){
        Button btn;

        btn = findViewById(R.id.btnSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveConfig();
            }
        });

        btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void saveConfig(){
        Configurations conf = new Configurations();
        TextView textView;
        int i = 0;

        textView = findViewById(R.id.editConstructionDrawingPath);
        conf.setConstructionDrawingPath(textView.getText().toString());

        textView = findViewById(R.id.editTechnicDatasheetPath);
        conf.setTechnicDatasheetPath(textView.getText().toString());

        textView = findViewById(R.id.editDrawingCode);
        conf.setDrawingCode(textView.getText().toString());

        textView = findViewById(R.id.editDatasheetCode);
        conf.setDatasheetCode(textView.getText().toString());

        textView = findViewById(R.id.editDrawingExtension);
        conf.setDrawingExtension(textView.getText().toString());

        textView = findViewById(R.id.editDatasheetExtension);
        conf.setDatasheetExtension(textView.getText().toString());

        textView = findViewById(R.id.editRootPath);
        conf.setRootPath(textView.getText().toString());

        textView = findViewById(R.id.editServerPath);
        conf.setServerPath(textView.getText().toString());

        textView = findViewById(R.id.editServerUsername);
        conf.setServerUsername(textView.getText().toString());

        textView = findViewById(R.id.editServerPassword);
        conf.setServerPassword(textView.getText().toString());

        textView = findViewById(R.id.editYearPrefix);
        conf.setYearPrefix(textView.getText().toString());

        textView = findViewById(R.id.editAppPassword);
        conf.setAppPassword(textView.getText().toString());


        for(Field f:conf.getClass().getDeclaredFields()){
            f.setAccessible(true);
            String fieldValue = (String)runGetter(f, new Configurations());
            if(fieldValue != null) {
                if (fieldValue.equals("")) {
                    i++;
                }
            }
            else{
                if(f != null && f.getClass().isAssignableFrom(String.class)) {
                    Toast.makeText(this, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        Toast.makeText(this, R.string.valuesSavedMessage, Toast.LENGTH_LONG).show();

        if(i == 0) {
            ConfigurationsManager.saveConfig(conf, this);
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
        // MZ: Find the correct method
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
                        e.printStackTrace();
                    }

                }
            }
        }


        return null;
    }
}
