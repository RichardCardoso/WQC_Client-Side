package com.richard.weger.wegerqualitycontrol.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class FileBytesHandler {
    public static byte[] execute(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int bytesRead;
        while((bytesRead = in.read(b)) != -1){
            bos.write(b, 0, bytesRead);
        }
        return bos.toByteArray();
    }
}
