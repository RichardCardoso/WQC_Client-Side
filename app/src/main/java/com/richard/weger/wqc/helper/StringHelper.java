package com.richard.weger.wqc.helper;

import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.util.App;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StringHelper {
    public static String getQrText(Project project){
        // qr_text_sample: \17-1-435_Z_1_T_1
        StringBuilder sb = new StringBuilder();
        String projectNumber = project.getReference();
        int drawingNumber = project.getDrawingRefs().get(0).getDnumber();
        int partNumber = project.getDrawingRefs().get(0).getParts().get(0).getNumber();
        sb.append('\\');
        sb.append(projectNumber);
        sb.append("_Z_");
        sb.append(drawingNumber);
        sb.append("_T_");
        sb.append(partNumber);
        return sb.toString();
    }

    public static String getStackTraceAsString(Exception e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String getProjectName(Project project){
        String qrText = getQrText(project);
        return getProjectName(qrText);
    }

    private static String getProjectName(String qrcode){
        int teilPos = qrcode.toLowerCase().indexOf("t");
        return qrcode.substring(1, teilPos - 1);
    }

    public static String getProjectFolderPath(Project project){
        File externalDir = App.getContext().getExternalFilesDir(null);
        if(externalDir != null)
            return externalDir.getPath() + "/" + getProjectName(project) + "/";
        else
            return null;
    }

    public static String getProjectFolderPath(String qrcode){
        File externalDir = App.getContext().getExternalFilesDir(null);
        if(externalDir != null)
            return externalDir.getPath() + "/" + getProjectName(qrcode) + "/";
        else
            return null;
    }

    public static String getPicturesFolderPath(Project project){
        String ret = getProjectFolderPath(project).concat(getPicturesFolderName()).concat("/");
        File f = new File(ret);
        f.mkdirs();
        return ret;
    }

    public static String getPicturesFolderPath(String qrcode){
        return getProjectFolderPath(qrcode).concat(getPicturesFolderName()).concat("/");
    }

    public static String getPictureFilePath(Project project, Item item){
        return getPicturesFolderPath(project).concat(item.getPicture().getFileName());
    }

    public static String getPdfsFolderPath(Project project){
        return getProjectFolderPath(project).concat(getPdfsFolderName()).concat("/");
    }

    public static String getPdfsFolderName(){
        return "Originals";
    }

    public static String getPicturesFolderName(){
        return "Pictures";
    }

    public static String getItemImageFilename(Item item){
        if(item != null) {
            return item.getPicture().getFileName();
        } else {
            return null;
        }
    }

    public static String getGeneralPictureFilename(Project project){
        String ret = null;
        if(project != null){
            ret = project.getReference()
                    .concat("Z").concat(String.valueOf(project.getDrawingRefs().get(0).getDnumber()))
                    .concat("T").concat(String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()))
                    .concat("QP").concat(String.valueOf(ProjectHelper.getCurrentPicNumber(project)));
            ret = ImageHelper.getTempPicName(ret);
        }
        return ret;
    }

}
