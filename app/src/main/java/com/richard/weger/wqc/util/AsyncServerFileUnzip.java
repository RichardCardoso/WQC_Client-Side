package com.richard.weger.wqc.util;

import android.os.AsyncTask;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class AsyncServerFileUnzip extends AsyncTask<Object, Integer, Boolean> {

    private AsyncServerFileUnzipResponse delegate;
    Configurations conf;

    public AsyncServerFileUnzip(AsyncServerFileUnzipResponse delegate){
        this.delegate = delegate;
    }

    public interface AsyncServerFileUnzipResponse{
        void AsyncServerFileUnzipCallback(boolean bResult);
        void AsyncServerFileProgressUpdate(int currentProgress);
    }

    @Override
    protected Boolean doInBackground(Object... objects) {
        this.conf = (Configurations) objects[1];
        return unpackZip((String)objects[0], (long)objects[2]);
    }

    private boolean unpackZip(String zipFullPath, long zipSize)
    {
        SmbFile in;
        SmbFileInputStream is;
        ZipInputStream zis;

        try {

            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, "" /*conf.getServerUsername()*/, ""/*conf.getServerPassword()*/);

            try {
                String filename;
                String rootExtractFolderName;
                rootExtractFolderName = zipFullPath.substring(0, zipFullPath.lastIndexOf("."));
                zipFullPath = "smb://".concat(zipFullPath);
                in = new SmbFile(zipFullPath, auth);
                is = new SmbFileInputStream(in);
                zis = new ZipInputStream(new BufferedInputStream(is));
                ZipEntry ze;
                byte[] buffer = new byte[1024];
                int count;
                int currentProgress = 0;

                while ((ze = zis.getNextEntry()) != null) {
                    // zapis do souboru
                    filename = ze.getName();

                    // Need to create directories if not exists, or
                    // it will generate an Exception...
                    String parentFolderPath = "smb://".concat(rootExtractFolderName + filename.substring(0, filename.lastIndexOf("/")));
                    SmbFile parentFolder = new SmbFile(parentFolderPath, auth);
                    if(!parentFolder.exists()){
                        parentFolder.mkdirs();
                    }

                    if (ze.isDirectory()) {
                        SmbFile fmd = new SmbFile(rootExtractFolderName + filename, auth);
                        fmd.mkdirs();
                        continue;
                    }

                    SmbFile outputFile = new SmbFile("smb://".concat(rootExtractFolderName) + filename, auth);

                    SmbFileOutputStream fout = new SmbFileOutputStream(outputFile);

                    // cteni zipu a zapis

                    while ((count = zis.read(buffer)) != -1) {
                        double totalProgress = (double)currentProgress / (double) zipSize;
                        totalProgress *= 100;
                        fout.write(buffer, 0, count);
                        currentProgress += count;
                        publishProgress((int) Math.round(totalProgress));
                    }

                    fout.close();
                    zis.closeEntry();
                }

                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result){
        delegate.AsyncServerFileUnzipCallback(result);
    }

    @Override
    protected void onProgressUpdate(Integer... progress){
        delegate.AsyncServerFileProgressUpdate(progress[0]);
    }
}
