package com.richard.weger.wqc.util;

import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;

import java.io.File;

import static com.richard.weger.wqc.util.AppConstants.*;

public class StringHandler {
    public static String createQrText(Project project){
        // qr_text_sample: \17-1-435_Z_1_T_1
        StringBuilder sb = new StringBuilder();
        String projectNumber = project.getReference();
        int drawingNumber = project.getDrawingRefs().get(0).getNumber();
        int partNumber = project.getDrawingRefs().get(0).getParts().get(0).getNumber();
        sb.append('\\');
        sb.append(projectNumber);
        sb.append("_Z_");
        sb.append(drawingNumber);
        sb.append("_T_");
        sb.append(partNumber);
        return sb.toString();
    }

    public static String generatePictureName(Project project, Item item, Report report){
        String string = createQrText(project);
        StringBuilder sb = new StringBuilder();
        sb.append(string.substring(1,string.length()));
        sb.append(report.toString().replace(' ', '-'));
        sb.append("-");
        sb.append(item.getNumber());
        sb.append("-");
        // sb.append(".jpg");
        return sb.toString();
    }

    public static String getProjectName(Project project){
        String qrText = createQrText(project);
        return qrText.substring(1, qrText.length());
    }

    public static String generateFileName(Project project){
        return getProjectName(project).concat(".").concat(FILE_EXTENSION);
    }

    public static String generateFileName(Project project, String extension){
        return getProjectName(project).concat(".").concat(extension);
    }

    public static String generateProjectFolderName(File externalDir, Project project){
        return externalDir.getPath() + "/" + getProjectName(project) + "/";
    }
}
