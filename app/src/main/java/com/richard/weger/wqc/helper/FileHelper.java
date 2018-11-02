package com.richard.weger.wqc.helper;

import android.net.Uri;

import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.richard.weger.wqc.constants.AppConstants.*;

public class FileHelper {

    public static boolean fileDelete(String fileName){
        File file = new File(fileName);
        try {
            file.delete();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public static boolean fileNameMatches(String entryData, ParamConfigurations conf, String fCode, String fExtension){
        String constCode = "";
        String constExtension = "";
        switch(entryData){
            case CONSTRUCTION_PATH_KEY: {
                constCode = conf.getConstructionDrawingCode();
                constExtension = conf.getOriginalDocsExtension();
                break;
            }
            case TECHNICAL_PATH_KEY: {
                constCode = conf.getDatasheetCode();
                constExtension = conf.getOriginalDocsExtension();
                break;
            }
        }
        if (fCode.equals(constCode)) {
            if (fExtension.equals(constExtension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidFile(String filePath){
        String path = Uri.parse(filePath).getPath();
        File file = new File(path);
        return (file.exists() && !(file.length() == 0));
    }

    public static void byteArray2File(String filePath, byte[] bytes) throws IOException {
        // Your ByteArrayInputStream here
        File rootPath = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        if(!rootPath.exists()){
            rootPath.mkdirs();
        }
        InputStream in = new ByteArrayInputStream(bytes);
        OutputStream out;
        out = new FileOutputStream(filePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


    public static void fileCopy(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }


}
