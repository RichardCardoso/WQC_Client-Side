package com.richard.weger.wqc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.richard.weger.wqc.R;

import java.util.Map;

import static com.richard.weger.wqc.util.AppConstants.*;

public class FileFromList{
    private int id;
    private String fileName;
    private String filePath;
    private Context context;
    private SharedPreferences mPrefs;

    public FileFromList(Context context, SharedPreferences mPrefs){
        setId(0);
        setFileName("");
        setFilePath("");
        this.setContext(context);
        this.setmPrefs(mPrefs);
    }

    @Override
    public String toString(){
        Map<String, String> mapValues;
        QrTextHandler qrTextHandler = new QrTextHandler(context, ConfigurationsManager.getServerConfig());
        StringBuilder sb = new StringBuilder();

        mapValues = qrTextHandler.execute("\\" +
                getFileName().substring(0,getFileName().lastIndexOf('.')));
        if(mapValues == null){
            Toast.makeText(context, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
            return null;
        }
        sb.append(this.context.getResources().getString(R.string.projectLabel));
        sb.append(": ");
        sb.append(mapValues.get(PROJECT_NUMBER_KEY));
        sb.append(", ");
        sb.append(this.context.getResources().getString(R.string.drawingLabel));
        sb.append(": ");
        sb.append(mapValues.get(DRAWING_NUMBER_KEY));
        sb.append(", ");
        sb.append(this.context.getResources().getString(R.string.partLabel));
        sb.append(": ");
        sb.append(mapValues.get(PART_NUMBER_KEY));
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public SharedPreferences getmPrefs() {
        return mPrefs;
    }

    public void setmPrefs(SharedPreferences mPrefs) {
        this.mPrefs = mPrefs;
    }
}
