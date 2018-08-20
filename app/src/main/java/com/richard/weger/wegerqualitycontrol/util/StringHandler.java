package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;

import com.richard.weger.wegerqualitycontrol.activity.ProjectMain;
import com.richard.weger.wegerqualitycontrol.domain.Item;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;

import java.io.File;
import java.util.Map;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public class StringHandler {
    public static String createQrText(Project project){
        // qr_text_sample: \17-1-435_Z_1_T_1
        StringBuilder sb = new StringBuilder();
        String projectNumber = project.getNumber();
        int drawingNumber = project.getDrawingList().get(0).getNumber();
        int partNumber = project.getDrawingList().get(0).getPart().get(0).getNumber();
        sb.append('\\');
        sb.append(projectNumber);
        sb.append("_Z_");
        sb.append(drawingNumber);
        sb.append("_T_");
        sb.append(partNumber);
        return sb.toString();
    }

    public static String generateFileName(Project project){
        String string = createQrText(project);
        return string.substring(1, string.length()).concat(".").concat(FILE_EXTENSION);
    }

    public static String generateFileName(Project project, String extension){
        String string = createQrText(project);
        return string.substring(1, string.length()).concat(".").concat(extension);
    }

    public static String generatePictureName(Project project, Item item, Report report){
        String string = createQrText(project);
        StringBuilder sb = new StringBuilder();
        sb.append(string.substring(1,string.length()));
        sb.append(report.toString().replace(' ', '-'));
        sb.append("-");
        sb.append(item.getId());
        sb.append("-");
        // sb.append(".jpg");
        return sb.toString();
    }

    public static String generateProjectFolderName(File externalDir, Project project){
        String qrText = createQrText(project);
        return externalDir.getPath() + "/" + qrText.substring(1, qrText.length()) + "/";
    }
}
