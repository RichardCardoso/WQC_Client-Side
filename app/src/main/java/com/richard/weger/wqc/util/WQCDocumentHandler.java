package com.richard.weger.wqc.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class WQCDocumentHandler {

    private static String handleFileList(File[] fileList, String entryData,ParamConfigurations conf){
        if(fileList == null){
            return null;
        }

        for (File f : fileList) {
            String fName = f.getName();
            String fCode = fName.substring(0, 4);
            String fExtension = fName.substring(fName.length() - 4, fName.length());
            if(FileHandler.fileNameMatches(entryData, conf, fCode, fExtension)){
                return f.getPath();
            }
        }
        return null;
    }

//    public static String getFilePath(String documentKey, Configurations conf, Map<String, String> mapValues){
//        String filePath;
//        File file, filesList[];
//        filePath = Environment.getExternalStorageDirectory().getPath() + "/Documents/" + conf.getRootPath() + mapValues.getContext(documentKey);
//        file = new File(filePath);
//        filesList = file.listFiles();
//        filePath = handleFileList(filesList, documentKey, conf);
//        return filePath;
//    }

    public static void bitmap2Pdf(String inputFilePath, String outputFilePath, Project project, String documentCode, Map<Integer, List<Mark>> markList, Resources resources){
        PdfDocument document = new PdfDocument();
        for(int i = 0; i < markList.size(); i++) {
            Bitmap bitmap;
            bitmap = WQCDocumentHandler.pageLoad(i, inputFilePath, resources);
            bitmap = WQCDocumentHandler.updatePointsDrawing(markList.get(i), bitmap, resources);

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), i + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawPaint(paint);

            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

            paint.setColor(Color.BLUE);
            canvas.drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);
        }
        // write the document content
        String outputFolder = outputFilePath.substring(0, outputFilePath.lastIndexOf("/"));
        File folderPath = new File(outputFolder);
        if(!folderPath.exists()){
            folderPath.mkdirs();
        }
        File filePath = new File(outputFilePath);
        if(filePath.exists()){
            filePath.delete();
        }
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close the document
        document.close();
    }

    public static Bitmap pageLoad(int pageNumber, String filePath, Resources resources){
        return PdfHandler.pdf2Bitmap(filePath, pageNumber, resources);
    }

    public static Bitmap bitmapCopy(Bitmap originalBitmap){
        return originalBitmap.copy(originalBitmap.getConfig(), true);
    }

    public static Bitmap updatePointsDrawing(List<Mark> markList, Bitmap originalBitmap, Resources resources) {
        if (markList != null) {
            Canvas canvas;

            Bitmap currentBitmap = bitmapCopy(originalBitmap);
            canvas = new Canvas(currentBitmap);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.YELLOW);
            paint.setTextSize(16);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Bold text
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text over picture

            canvas.drawBitmap(originalBitmap, 0, 0, paint);
            for (Mark mark : markList) {
                Float x = mark.getX(),
                        y = mark.getY();
                drawMark(mark, canvas, paint, resources);
            }
            return currentBitmap;
        }
        return null;
    }

    private static void drawMark(Mark mark, Canvas canvas, Paint paint, Resources resources) {
        float[] touchPoint = new float[]{mark.getX(), mark.getY()};
        int radius = 18;
        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text over picture
        canvas.drawCircle(touchPoint[0] + radius / 2, touchPoint[1] - radius / 2 + 4, radius, circlePaint);
        canvas.drawText(mark.getDevice().getRole(), touchPoint[0], touchPoint[1], paint);
    }
}
