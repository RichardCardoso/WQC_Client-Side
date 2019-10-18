package com.richard.weger.wqc.helper;

import android.net.Uri;

import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.*;

public class FileHelper {

    private static Logger logger = LoggerManager.getLogger(FileHelper.class);

    public static boolean fileDelete(String fileName){
        File file = new File(fileName);
        try {
            file.delete();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public static boolean fileNameMatches(String entryData, ParamConfigurations conf, String fCode, String fExtension){
        String constCode = "";
        String constExtension = "";
        switch(entryData){
            case CONSTRUCTION_PATH_KEY: {
                constCode = conf.getWiredDrawingCode();
                constExtension = conf.getOriginalDocsExtension();
                break;
            }
            case TECHNICAL_PATH_KEY: {
                constCode = conf.getWiredDatasheetCode();
                constExtension = conf.getOriginalDocsExtension();
                break;
            }
        }
        if (fCode.equals(constCode)) {
            if (fExtension.equals(constExtension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidFile(String filePath){
        String path = Uri.parse(filePath).getPath();
        File file = new File(path);
        boolean result = file.exists();
        result &= (!(file.length() == 0));
        return result;
    }

    static void byteArray2File(String filePath, byte[] bytes) {
        // Your ByteArrayInputStream here
        File rootPath = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        if(!rootPath.exists()){
            rootPath.mkdirs();
        }
        InputStream in = new ByteArrayInputStream(bytes);
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                try {
                    out.write(buf, 0, len);
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            logger.warning(e.getMessage());
        } catch (IOException e) {
            logger.warning(e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
    }


    public static void fileCopy(File source, File dest) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            logger.warning(e.getMessage());
        } catch (IOException e) {
            logger.warning(e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
    }


}
