package com.richard.weger.wqc.util;

import android.os.AsyncTask;

import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class AsyncFromServerToLocalFile extends AsyncTask<Object, Void, Boolean> {

    String entryData;
    ParamConfigurations conf;
    File localFile;
    public AsyncFromServerToLocalFileResponse delegate;

    public AsyncFromServerToLocalFile(AsyncFromServerToLocalFileResponse delegate, ParamConfigurations conf){
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

    public interface AsyncFromServerToLocalFileResponse {
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
