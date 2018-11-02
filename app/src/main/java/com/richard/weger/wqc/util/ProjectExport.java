package com.richard.weger.wqc.util;

import android.os.AsyncTask;

import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;

public class ProjectExport extends AsyncTask<Object, Integer, String> {

    @Override
    protected String doInBackground(Object... objects) {
        return export();
    }

    public interface ProjectHandlerResponse {
        void ProjectHandlerCallback(String sResult);
        void ProjectHandlerProgressUpdate(int currentProgress);
    }

    public static ProjectExport.ProjectHandlerResponse delegate;

    public ProjectExport(ProjectExport.ProjectHandlerResponse delegate){
        ProjectExport.delegate = delegate;
    }

    private String export(){
        return null;
    }

    @Override
    public void onPostExecute(String result){
        delegate.ProjectHandlerCallback(result);
    }

    @Override
    public void onProgressUpdate(Integer... currentProgress){
        delegate.ProjectHandlerProgressUpdate(currentProgress[0]);
    }
}
