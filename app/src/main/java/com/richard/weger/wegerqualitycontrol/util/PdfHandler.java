package com.richard.weger.wegerqualitycontrol.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class PdfHandler {
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
        File file = new File(filePath);

        ParcelFileDescriptor mFileDescriptor = null;
        try{
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        PdfRenderer mPdfRenderer = null;
        try{
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        } catch (IOException e){
            e.printStackTrace();
        }

        return mPdfRenderer.getPageCount();
    }
}
