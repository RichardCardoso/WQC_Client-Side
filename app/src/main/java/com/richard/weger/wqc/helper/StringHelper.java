package com.richard.weger.wqc.helper;

import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.util.App;

import java.io.File;

public class StringHelper {
    public static String getQrText(Project project){
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

    private static String getProjectName(Project project){
        String qrText = getQrText(project);
        return qrText.substring(1, qrText.length());
    }

    public static String getProjectFolderPath(Project project){
        File externalDir = App.getContext().getExternalFilesDir(null);
        if(externalDir != null)
            return externalDir.getPath() + "/" + getProjectName(project) + "/";
        else
            return null;
    }

    public static String getPicturesFolderPath(Project project){
        return getProjectFolderPath(project).concat("Pictures/");
    }

    public static String getPictureFilePath(Project project, Item item){
        return getPicturesFolderPath(project).concat(item.getPicture().getFileName());
    }
}
