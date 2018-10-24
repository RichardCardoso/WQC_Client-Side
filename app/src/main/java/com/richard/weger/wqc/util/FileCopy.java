package com.richard.weger.wqc.util;

import android.content.Context;

import java.io.InputStream;
import java.io.OutputStream;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public abstract class FileCopy {

    public static void copyFromDevice(Context context, String localPath, String externalPath) throws Exception {
        SmbFile serverFile = new SmbFile(localPath);
        InputStream in = new SmbFileInputStream(serverFile);
        OutputStream out = context.openFileOutput(externalPath, Context.MODE_PRIVATE);
        byte[] buffer = new byte[in.available()];

        in.read(buffer);
        out.write(buffer);
    }

    public static void copyToDevice(Context context, String localPath, String externalPath) throws Exception {
        SmbFile serverFile  = new SmbFile(externalPath);
        InputStream in = new SmbFileInputStream(serverFile);
        OutputStream out = context.openFileOutput(localPath, Context.MODE_PRIVATE);
        byte[] buffer = new byte[in.available()];

        in.read(buffer);
        out.write(buffer);
    }
}
