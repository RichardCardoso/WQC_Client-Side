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
/*
        WorkbookHandler.handleWorkbook(
                res,
                externalFilesDir,
                project,
                b.getString(CONTROL_CARD_REPORT_FILE_KEY)
        );

        String inputPath = project.getDrawingList().getContext(0).getOriginalFileLocalPath();
        WQCDocumentHandler.bitmap2Pdf(inputPath,
                StringHandler.generateProjectFolderName(externalFilesDir, project)
                        + conf.getDrawingCode() + "-" +
                        StringHandler.generateFileName(project, "pdf"), project,
                conf.getDrawingCode(),
                project.getDrawingList().getContext(0).getDocumentMarks(), res);

        publishProgress(50);

        inputPath = project.getDrawingList().getContext(0).getDatasheet().getOriginalFileLocalPath();
        WQCDocumentHandler.bitmap2Pdf(inputPath,
                StringHandler.generateProjectFolderName(externalFilesDir, project)
                        + conf.getDatasheetCode() + "-" +
                        StringHandler.generateFileName(project, "pdf"), project,
                conf.getDatasheetCode(),
                project.getDrawingList().getContext(0).getDatasheet().getDocumentMarks(), res);

        publishProgress(50);

        inputPath = StringHandler.generateProjectFolderName(externalFilesDir, project);
        return inputPath;
*/
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
