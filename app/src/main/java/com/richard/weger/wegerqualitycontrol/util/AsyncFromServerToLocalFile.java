package com.richard.weger.wegerqualitycontrol.util;

import android.os.AsyncTask;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;

import org.apache.commons.io.IOUtils;

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
    Configurations conf;
    File localFile;
    public AsyncFromServerToLocalFile.AsyncSmbInStreamResponse delegate;

    public AsyncFromServerToLocalFile(AsyncFromServerToLocalFile.AsyncSmbInStreamResponse delegate, Configurations conf){
        jcifs.Config.setProperty("resolveOrder", "DNS");
        this.delegate = delegate;
        this.conf = conf;
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        SmbFile serverFile = (SmbFile) objects[0];
        entryData = (String) objects[1];
        localFile = (File) objects[2];
        return sendFileToServer(serverFile, localFile);
    }

    public interface AsyncSmbInStreamResponse {
        void AsyncSmbInStreamCallback(boolean bResult, String entryData, String localPath);
    }

    private boolean sendFileToServer(SmbFile serverFile, File localFile){

        FileOutputStream fos;
        SmbFileInputStream in;

        try {
            fos = new FileOutputStream(localFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            in = new SmbFileInputStream(serverFile);
        } catch (SmbException | MalformedURLException | UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        try {
            fos.write(IOUtils.toByteArray(in));
        } catch (IOException e) {
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
        delegate.AsyncSmbInStreamCallback(bResult, entryData, localFile.getPath());
    }
}
