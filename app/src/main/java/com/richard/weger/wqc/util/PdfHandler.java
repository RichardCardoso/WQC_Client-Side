package com.richard.weger.wqc.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class PdfHandler {
    public static void byteArray2Pdf(String filePath, byte[] bytes) throws IOException {
        // Your ByteArrayInputStream here
        File rootPath = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        if(!rootPath.exists()){
            rootPath.mkdirs();
        }
        InputStream in = new ByteArrayInputStream(bytes);
        OutputStream out;
        out = new FileOutputStream(filePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    public static Bitmap pdf2Bitmap(String filePath, int pageNumber, Resources resources){
        File file = new File(filePath);

        ParcelFileDescriptor mFileDescriptor = null;
        try{
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }

        PdfRenderer mPdfRenderer = null;
        try{
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        } catch (IOException e){
            e.printStackTrace();
            FileHandler.fileDelete(filePath);
            return null;
        }

        PdfRenderer.Page mCurrentPage = mPdfRenderer.openPage(pageNumber);
        Bitmap bitmap = Bitmap.createBitmap(resources.getDisplayMetrics().densityDpi * mCurrentPage.getWidth() / 144,
                                            resources.getDisplayMetrics().densityDpi * mCurrentPage.getHeight() / 144,
                                                    Bitmap.Config.ARGB_8888);

        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        mCurrentPage.close();
        mPdfRenderer.close();
        try{
            mFileDescriptor.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return bitmap;
    }

    public static int getPageCount(String filePath){
        if(!FileHandler.isValidFile(filePath))
            return 0;

        File file = new File(filePath);

        ParcelFileDescriptor mFileDescriptor = null;
        try{
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return 0;
        }

        PdfRenderer mPdfRenderer = null;
        try{
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        } catch (IOException e){
            e.printStackTrace();
            return 0;
        }

        return mPdfRenderer.getPageCount();
    }
}
