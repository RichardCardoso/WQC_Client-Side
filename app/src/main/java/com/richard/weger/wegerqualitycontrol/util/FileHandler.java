package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Configurations;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.CONSTRUCTION_PATH_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.TECHNICAL_PATH_KEY;

public class FileHandler {

    public static boolean fileNameMatches(String entryData, Configurations conf, String fCode, String fExtension){
        String constCode = "";
        String constExtension = "";
        switch(entryData){
            case CONSTRUCTION_PATH_KEY: {
                constCode = conf.getDrawingCode();
                constExtension = conf.getDrawingExtension();
                break;
            }
            case TECHNICAL_PATH_KEY: {
                constCode = conf.getDatasheetCode();
                constExtension = conf.getDatasheetExtension();
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

    public static boolean fileSave(Context context, String fileName, String data){
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

    public static String fileLoadAsString(Context context, String fileName){
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
}
