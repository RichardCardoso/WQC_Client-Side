package com.richard.weger.wqc.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.QrTextHelper;

import java.util.Map;

import static com.richard.weger.wqc.appconstants.AppConstants.DRAWING_NUMBER_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PART_NUMBER_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_NUMBER_KEY;

public class FileFromList{
    private int id;
    private String fileName;
//    private String filePath;
    private Context context;
//    private SharedPreferences mPrefs;

    public FileFromList(Context context){
        setId(0);
        setFileName("");
//        setFilePath("");
        this.setContext(context);
//        this.setmPrefs(mPrefs);
    }

    @NonNull
    @Override
    public String toString(){
        Map<String, String> mapValues;
        QrTextHelper qrTextHelper = new QrTextHelper(ConfigurationsManager.getServerConfig());
        StringBuilder sb = new StringBuilder();

        mapValues = qrTextHelper.execute("\\" +
                getFileName().substring(0,getFileName().lastIndexOf('.')));
        if(mapValues == null){
            Toast.makeText(context, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
            return "";
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

//    public String getFilePath() {
//        return filePath;
//    }

//    public void setFilePath(String filePath) {
//        this.filePath = filePath;
//    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

//    public SharedPreferences getmPrefs() {
//        return mPrefs;
//    }

//    private void setmPrefs(SharedPreferences mPrefs) {
//        this.mPrefs = mPrefs;
//    }
}
