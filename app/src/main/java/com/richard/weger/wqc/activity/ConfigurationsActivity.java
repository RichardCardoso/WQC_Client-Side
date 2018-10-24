package com.richard.weger.wqc.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.richard.weger.wqc.util.LogHandler.writeData;

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
        Configurations conf = ConfigurationsManager.getLocalConfig();

        textView = findViewById(R.id.editServerPath);
        textView.setText(conf.getServerPath());

        writeData("Finished filling configurations activity fields", getExternalFilesDir(null));
    }

    private void setListeners(){
        Button btn;

        writeData("Started setting configurations activity listeners", getExternalFilesDir(null));
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
        writeData("Finished setting configurations activity listeners", getExternalFilesDir(null));
    }

    private void saveConfig(){
        Configurations conf = new Configurations();
        TextView textView;
        int i = 0;

        writeData("Retrieving configurations activity fields values", getExternalFilesDir(null));

        textView = findViewById(R.id.editServerPath);
        conf.setServerPath(textView.getText().toString());

        writeData("Validating configurations activity fields values", getExternalFilesDir(null));
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
        writeData("Finished validating configurations activity fields values", getExternalFilesDir(null));

        if(i == 0) {
            writeData("Requesting project's json export", getExternalFilesDir(null));
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
