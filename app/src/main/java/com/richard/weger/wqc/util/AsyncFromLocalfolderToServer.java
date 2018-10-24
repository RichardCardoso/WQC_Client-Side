package com.richard.weger.wqc.util;

import android.os.AsyncTask;


//import org.apache.commons.io.IOUtils;

import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class AsyncFromLocalfolderToServer extends AsyncTask<Object, Integer, Boolean> {

    ParamConfigurations conf;
    private String localPath;
    private String serverPath;
    private int filesCount = 0,
        processedCount = 0;
    private AsyncFromLocalfolderToServerResponse delegate;

    public AsyncFromLocalfolderToServer(AsyncFromLocalfolderToServerResponse delegate, ParamConfigurations conf){
        jcifs.Config.setProperty("resolveOrder", "DNS");
        this.delegate = delegate;
        this.conf = conf;
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        serverPath = (String) objects[0];
        localPath = (String) objects[1];
        return recursiveCopy(serverPath, localPath);
    }

    public interface AsyncFromLocalfolderToServerResponse {
        void AsyncFromLocalfolderToServerCallback(boolean bResult, String serverPath);
        void AsyncFromLocalfolderToServerProgressUpdate(int currentProgress);
    }

    private static int getFilesCount(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File f : files)
            if (f.isDirectory())
                count += getFilesCount(f);
            else
                count++;

        return count;
    }

    private boolean recursiveCopy(String serverDirPath, String localDirPath) {
        SmbFile outputFile = null;
        File inputFile = null;

        SmbFileOutputStream fos = null;
        FileInputStream in = null;

        String serverFolder;
        SmbFile folder;

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, ""/*conf.getServerUsername()*/, ""/*conf.getServerPassword()*/);

        inputFile = new File(localDirPath);

        if(filesCount == 0){
            filesCount = getFilesCount(inputFile);
        }

        if(inputFile.isDirectory()){
            if(inputFile.listFiles() != null){
                for(File file : inputFile.listFiles()){
                    if(file.isDirectory()){
                        if(!makeDir(serverDirPath + File.separator + file.getName(), auth)) {
                            continue;
                        }
                    }
                    recursiveCopy(serverDirPath + File.separator + file.getName(), inputFile.getPath() + File.separator + file.getName());
                }
            }
        } else {
            try {
                outputFile = new SmbFile(serverDirPath, auth);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }

            try {
                fos = new SmbFileOutputStream(outputFile);
            } catch (UnknownHostException | SmbException | MalformedURLException e) {
                e.printStackTrace();
                return false;
            }

            try {
                in = new FileInputStream(inputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            try {
                fos.write(FileBytesHandler.execute(in));
                //fos.write(IOUtils.toByteArray(in));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        publishProgress(Math.round((float)processedCount++ / (float)filesCount * 100));
        return true;
    }

    private boolean makeDir(String dirPath, NtlmPasswordAuthentication auth){
        try {
            SmbFile outputDir = new SmbFile(dirPath, auth);
            if(!outputDir.exists())
                outputDir.mkdirs();
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean bResult) {
        delegate.AsyncFromLocalfolderToServerCallback(bResult, serverPath);
    }

    @Override
    public void onProgressUpdate(Integer... currentProgress){
        delegate.AsyncFromLocalfolderToServerProgressUpdate(currentProgress[0]);
    }
}
