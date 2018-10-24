package com.richard.weger.wqc.util;

import android.content.Context;
import android.net.Uri;

import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.richard.weger.wqc.util.AppConstants.CONSTRUCTION_PATH_KEY;
import static com.richard.weger.wqc.util.AppConstants.TECHNICAL_PATH_KEY;

public class FileHandler{

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

    public static boolean localFileSave(Context context, String fileName, String data){
        FileOutputStream fos;

        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            try {
                fos.write(data.getBytes());
                fos.close();
                return true;
            } catch(IOException e){
                e.printStackTrace();
                return false;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String localFileLoadAsString(Context context, String fileName){
        FileInputStream fis;
        BufferedReader br;
        StringBuilder res = new StringBuilder();
        String line;
        String input;

        try{
            fis = context.openFileInput(fileName);
            br = new BufferedReader(new InputStreamReader(fis));
            while ((line = br.readLine()) != null){
                res.append(line);
            }
            input = res.toString();
            return input;
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isValidFile(String filePath){
        String path = Uri.parse(filePath).getPath();
        File file = new File(path);
        return (file.exists() && !(file.length() == 0));
    }
}
