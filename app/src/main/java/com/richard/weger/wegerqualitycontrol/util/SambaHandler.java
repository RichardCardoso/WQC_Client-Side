package com.richard.weger.wegerqualitycontrol.util;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.richard.weger.wegerqualitycontrol.domain.Configurations;

import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SambaHandler extends AsyncTask<String, Void, SmbFile[]>{

    String entryData;
    Configurations conf;

    public interface AsyncResponse {
        void processFinish(SmbFile[] output, String input);
    }

    public AsyncResponse delegate;

    @Override
    protected void onPostExecute(SmbFile[] result) {
        delegate.processFinish(result, entryData);
    }

    public SambaHandler(AsyncResponse delegate, Configurations conf){
        jcifs.Config.setProperty("resolveOrder", "DNS");
        this.delegate = delegate;
        this.conf = conf;
    }

    @Override
    protected SmbFile[] doInBackground(String... strings) {
        String path = strings[0];
        entryData = strings[1];
        return getFileList(path);
    }

    private NtlmPasswordAuthentication authenticate(){
        String username = conf.getServerUsername();
        String password = conf.getServerPassword();
        return new NtlmPasswordAuthentication(
                null,
                username, password);
    }

    private SmbFile[] getFileList(String path){
        NtlmPasswordAuthentication authentication = authenticate();
        SmbFile currentFolder = null;
        try {
            String serverPath = conf.getServerPath();
            String rootPath = conf.getRootPath();
            String url = "smb://" + serverPath + rootPath + path;
            currentFolder = new SmbFile(url, authentication);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if(currentFolder != null){
            try {
                return currentFolder.listFiles();
            } catch (SmbException e) {
                e.printStackTrace();
                return null;
            }
        }
        else{
            return null;
        }
    }

}
