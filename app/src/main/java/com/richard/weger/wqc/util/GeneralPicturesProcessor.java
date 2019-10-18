package com.richard.weger.wqc.util;

import android.os.AsyncTask;

import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.ImageHelper;

public class GeneralPicturesProcessor extends AsyncTask<String, Void, String> {

    private GeneralPicturesProcessorScheduler listener;
    private Project project;

    GeneralPicturesProcessor(GeneralPicturesProcessorScheduler listener, Project project) {
        this.listener = listener;
        this.project = project;
    }

    @Override
    protected String doInBackground(String... strings) {
        String temporaryPath = strings[0];

        final String concretePath = ImageHelper.getFinalFilepath(temporaryPath);
        ImageHelper.compressImage(temporaryPath);

        String prefixedNumber = ImageHelper.getFinalFilename(concretePath);
        prefixedNumber = prefixedNumber.substring(prefixedNumber.indexOf("QP"));
        if(prefixedNumber.contains(".")){
            prefixedNumber = prefixedNumber.substring(0, prefixedNumber.indexOf("."));
        }
        ImageHelper.putPicInfoAndTimestamp(concretePath, project, prefixedNumber);

        return temporaryPath;
    }

    @Override
    protected void onPostExecute(String s) {
        listener.doNext(s);
    }

}
