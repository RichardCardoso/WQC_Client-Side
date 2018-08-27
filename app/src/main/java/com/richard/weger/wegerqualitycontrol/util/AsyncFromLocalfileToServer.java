package com.richard.weger.wegerqualitycontrol.util;

import android.os.AsyncTask;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;

//import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
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

public class AsyncFromLocalfileToServer extends AsyncTask<Object, Void, Boolean> {

    String entryData;
    Configurations conf;
    String localPath;
    public AsyncFromLocalfileToServer.AsyncFromLocalfileToServerResponse delegate;

    public AsyncFromLocalfileToServer(AsyncFromLocalfileToServer.AsyncFromLocalfileToServerResponse delegate, Configurations conf){
        jcifs.Config.setProperty("resolveOrder", "DNS");
        this.delegate = delegate;
        this.conf = conf;
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        String serverPath = (String) objects[0];
        entryData = (String) objects[1];
        localPath = (String) objects[2];
        return sendFileToServer(serverPath, localPath);
    }

    public interface AsyncFromLocalfileToServerResponse {
        void AsyncFromLocalfileToServerCallback(boolean bResult, String entryData, String localPath);
    }

    private boolean sendFileToServer(String serverPath, String lPath){

        SmbFile outputFile=null;
        File inputFile=null;

        SmbFileOutputStream fos=null;
        FileInputStream in=null;

        serverPath = "smb://".concat(serverPath);
        // lPath = "smb://".concat(lPath);

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, conf.getServerUsername(), conf.getServerPassword());

        try {
            outputFile  = new SmbFile(serverPath, auth);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        inputFile = new File(lPath);

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
        return true;
    }

    @Override
    protected void onPostExecute(Boolean bResult) {
        delegate.AsyncFromLocalfileToServerCallback(bResult, entryData, localPath);
    }
}
