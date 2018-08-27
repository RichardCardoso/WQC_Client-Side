package com.richard.weger.wegerqualitycontrol.util;

import android.os.AsyncTask;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;

//import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class AsyncFromServerToLocalfile extends AsyncTask<Object, Void, Boolean> {

    String entryData;
    Configurations conf;
    File localFile;
    public AsyncFromServerToLocalfileResponse delegate;

    public AsyncFromServerToLocalfile(AsyncFromServerToLocalfileResponse delegate, Configurations conf){
        jcifs.Config.setProperty("resolveOrder", "DNS");
        this.delegate = delegate;
        this.conf = conf;
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        SmbFile serverFile = (SmbFile) objects[0];
        entryData = (String) objects[1];
        localFile = (File) objects[2];
        return getFileFromServer(localFile, serverFile);
    }

    public interface AsyncFromServerToLocalfileResponse {
        void AsyncFileFromServerCallback(boolean bResult, String entryData, String localPath);
    }

    private boolean getFileFromServer(File localFile, SmbFile serverFile){
        SmbFileInputStream in = null;
        FileOutputStream fos = null;

        try {
            in = new SmbFileInputStream(serverFile);
        } catch (UnknownHostException | SmbException | MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            fos = new FileOutputStream(localFile);
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
        delegate.AsyncFileFromServerCallback(bResult, entryData, localFile.getPath());
    }
}