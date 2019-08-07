package com.richard.weger.wqc.helper;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.util.App;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.SDF;

public abstract class LogHelper {
    public static boolean writeData(String data){
        File externalFilesDir = App.getContext().getExternalFilesDir(null);
        if(externalFilesDir == null){
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        File file = new File(externalFilesDir.getPath().concat("/log-").concat(sdf.format(Calendar.getInstance().getTime())).concat(".txt"));
        Writer out;
        Date currentTime = Calendar.getInstance().getTime();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(file.exists()){
            try {
                StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
                String message = "Unknown error has ocurred";
                if(data != null){
                    message = data;
                }
                out = new BufferedWriter(new FileWriter(file,true), 1024);
                out.write(
                        SDF.format(currentTime)
                                .concat(": ")
                                .concat(message)
                                .concat("\n")
                                .concat(Arrays.stream(stackTraceElements)
                                        .filter(t -> t.getClassName().startsWith("com.richard.weger.wqc") && !t.getClassName().contains("LogHelper"))
                                        .map(t -> "\t"
                                                .concat(t.getClassName().substring(t.getClassName().lastIndexOf(".") + 1))
                                                .concat(".")
                                                .concat(t.getMethodName())
                                                .concat(", line: ")
                                                .concat(String.valueOf(t.getLineNumber())))
                                        .collect(Collectors.joining("\n")))
                                .concat("\n")
                );
                out.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
