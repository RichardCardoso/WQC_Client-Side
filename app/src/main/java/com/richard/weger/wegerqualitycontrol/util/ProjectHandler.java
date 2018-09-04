package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;
import com.richard.weger.wegerqualitycontrol.domain.Project;

import java.io.File;
import java.util.Map;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.CONTROL_CARD_REPORT_FILE_KEY;

public class ProjectHandler implements Runnable{

    private static Resources res;
    private static Bundle b;
    private static File externalFilesDir;
    private static Configurations conf;
    private static Project project;

    public interface ProjectHandlerResponse {
        public void ProjectHandlerCallback(String sResult);
    }

    public static ProjectHandler.ProjectHandlerResponse delegate;

    public ProjectHandler (Resources res, Bundle b, File externalFilesDir, Configurations conf, Project project,  ProjectHandler.ProjectHandlerResponse delegate){
        ProjectHandler.res = res;
        ProjectHandler.b = b;
        ProjectHandler.externalFilesDir = externalFilesDir;
        ProjectHandler.conf = conf;
        ProjectHandler.project = project;
        ProjectHandler.delegate = delegate;
    }

    private static String export(){

        WorkbookHandler.handleWorkbook(
                res,
                externalFilesDir,
                project,
                b.getString(CONTROL_CARD_REPORT_FILE_KEY)
        );

        String inputPath = project.getDrawingList().get(0).getOriginalFileLocalPath();
        WQCDocumentHandler.bitmap2Pdf(inputPath,
                StringHandler.generateProjectFolderName(externalFilesDir, project)
                        + conf.getDrawingCode() + "-" +
                        StringHandler.generateFileName(project, "pdf"), project,
                conf.getDrawingCode(),
                project.getDrawingList().get(0).getDocumentMarks(), res);

        inputPath = project.getDrawingList().get(0).getDatasheet().getOriginalFileLocalPath();
        WQCDocumentHandler.bitmap2Pdf(inputPath,
                StringHandler.generateProjectFolderName(externalFilesDir, project)
                        + conf.getDatasheetCode() + "-" +
                        StringHandler.generateFileName(project, "pdf"), project,
                conf.getDatasheetCode(),
                project.getDrawingList().get(0).getDatasheet().getDocumentMarks(), res);

        inputPath = StringHandler.generateProjectFolderName(externalFilesDir, project);
        if(inputPath.charAt(inputPath.length() - 1) == '/' || inputPath.charAt(inputPath.length() - 1) == '\\'){
            inputPath = inputPath.substring(0,inputPath.length() - 1);
        }

        ZipHelper.zipFileAtPath(inputPath, inputPath.concat(".zip"));

        return inputPath;

    }

    @Override
    public void run() {
        delegate.ProjectHandlerCallback(export());
    }
}
