package com.richard.weger.wegerqualitycontrol.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.SDF;

public abstract class LogHandler {
    public static boolean writeData(String data, File externalFilesDir){
        if(externalFilesDir == null){
            return false;
        }
        File file = new File(externalFilesDir.getPath().concat("/log.txt"));
        Writer out;
        Date currentTime = Calendar.getInstance().getTime();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(file.exists()){
            try {
                out = new BufferedWriter(new FileWriter(file,true), 1024);
                out.write(SDF.format(currentTime).concat(": ").concat(data).concat("\n"));
                out.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
