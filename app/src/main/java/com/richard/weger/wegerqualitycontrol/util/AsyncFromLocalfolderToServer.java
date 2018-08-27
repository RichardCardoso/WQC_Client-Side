package com.richard.weger.wegerqualitycontrol.util;

import android.os.AsyncTask;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;

//import org.apache.commons.io.IOUtils;

import java.net.MalformedURLException;
import java.util.Objects;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class AsyncFromLocalfolderToServer extends AsyncTask<Object, Void, Boolean> {

    Configurations conf;
    String localPath;
    String serverPath;
    public AsyncFromLocalfolderToServer.AsyncSmbFolderToServer delegate;

    public AsyncFromLocalfolderToServer(AsyncFromLocalfolderToServer.AsyncSmbFolderToServer delegate, Configurations conf){
        jcifs.Config.setProperty("resolveOrder", "DNS");
        this.delegate = delegate;
        this.conf = conf;
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        serverPath = (String) objects[0];
        localPath = (String) objects[1];
        return sendFileToServer(serverPath, localPath);
    }

    public interface AsyncSmbFolderToServer {
        void AsyncSmbFolderToServerCallback(boolean bResult, String serverPath);
    }

    private boolean sendFileToServer(String serverPath, String localPath){

        SmbFile smbIn = null;
        SmbFile smbOut = null;

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, conf.getServerUsername(), conf.getServerPassword());

        jcifs.Config.registerSmbURLHandler();

        try {
            localPath = ("smb://").concat(localPath);
            smbIn = new SmbFile(localPath, new NtlmPasswordAuthentication(null));
            serverPath = ("smb://").concat(serverPath);
            smbOut = new SmbFile(serverPath, auth);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            if(!smbOut.exists()){
                smbOut.mkdirs();
            }
//            smbIn.copyTo(new SmbFile(smbIn.getURL().toString().concat("_test")));
            Objects.requireNonNull(smbIn).copyTo(smbOut);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean bResult) {
        delegate.AsyncSmbFolderToServerCallback(bResult, serverPath);
    }
}
