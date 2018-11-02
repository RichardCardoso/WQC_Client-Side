package com.richard.weger.wqc.util;

import android.os.AsyncTask;

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

public class AsyncFromLocalfileToServer extends AsyncTask<Object, Void, String> {

    String entryData;
    Configurations conf;
    String localPath;
    public AsyncFromLocalfileToServerResponse delegate;

    public AsyncFromLocalfileToServer(AsyncFromLocalfileToServerResponse delegate, Configurations conf){
        jcifs.Config.setProperty("resolveOrder", "DNS");
        this.delegate = delegate;
        this.conf = conf;
    }

    @Override
    protected String doInBackground(Object... objects) {
        String serverPath = (String) objects[0];
        entryData = (String) objects[1];
        localPath = (String) objects[2];
        return sendFileToServer(serverPath, localPath);
    }

    public interface AsyncFromLocalfileToServerResponse {
        void AsyncFromLocalfileToServerCallback(String bResult, String entryData, String localPath);
    }

    private String sendFileToServer(String serverPath, String lPath){

        SmbFile outputFile=null;
        File inputFile=null;

        SmbFileOutputStream fos=null;
        FileInputStream in=null;

        String serverFolder;
        SmbFile folder;

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, ""/*conf.getServerUsername()*/, ""/*conf.getServerPassword()*/);

        serverPath = "smb://".concat(serverPath);
        serverFolder = serverPath.substring(0, serverPath.lastIndexOf("/"));

        try {
            folder = new SmbFile(serverFolder, auth);
            if(!folder.exists()){
                folder.mkdirs();
            }
        } catch (MalformedURLException | SmbException e) {
            e.printStackTrace();
        }

        try {
            outputFile  = new SmbFile(serverPath, auth);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        inputFile = new File(lPath);

        try {
            fos = new SmbFileOutputStream(outputFile);
        } catch (UnknownHostException | SmbException | MalformedURLException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        try {
            in = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        try {
            fos.write(FileBytesHandler.execute(in));
            //fos.write(IOUtils.toByteArray(in));
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String sResult) {
        delegate.AsyncFromLocalfileToServerCallback(sResult, entryData, localPath);
    }
}