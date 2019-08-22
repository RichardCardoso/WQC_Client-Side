package com.richard.weger.wqc.helper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.richard.weger.wqc.util.LoggerManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class PdfHelper {

    private static Logger logger = LoggerManager.getLogger(PdfHelper.class);

    public static Bitmap pdf2Bitmap(String filePath, int pageNumber, Resources resources){
        File file = new File(filePath);

        ParcelFileDescriptor mFileDescriptor = null;
        try{
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e){
            logger.warning(e.toString());
            return null;
        }

        PdfRenderer mPdfRenderer = null;
        try{
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        } catch (IOException e){
            logger.warning(e.toString());
            FileHelper.fileDelete(filePath);
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
            logger.warning(e.toString());
        }
        return bitmap;
    }

    public static int getPageCount(String filePath){
        if(!FileHelper.isValidFile(filePath))
            return 0;

        File file = new File(filePath);

        ParcelFileDescriptor mFileDescriptor = null;
        try{
            mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e){
            logger.warning(e.toString());
            return 0;
        }

        PdfRenderer mPdfRenderer = null;
        try{
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        } catch (IOException e){
            logger.warning(e.toString());
            return 0;
        }

        return mPdfRenderer.getPageCount();
    }
}
