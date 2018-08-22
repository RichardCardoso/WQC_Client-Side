package com.richard.weger.wegerqualitycontrol.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.CONSTRUCTION_PATH_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.TECHNICAL_PATH_KEY;

public class FileHandler{

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

    public static boolean fileToServer(SmbFile remoteFile, File localFile){
        SmbFileOutputStream out;
        FileInputStream fis;

        try {
            out = new SmbFileOutputStream(remoteFile);
        } catch (SmbException | MalformedURLException | UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        try {
            fis = new FileInputStream(localFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            out.write(IOUtils.toByteArray(fis));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
