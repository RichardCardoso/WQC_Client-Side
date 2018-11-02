package com.richard.weger.wqc.helper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class WQCDocumentHelper {

    public static int radius = 18;


    public static Bitmap pageLoad(int pageNumber, String filePath, Resources resources){
        return PdfHelper.pdf2Bitmap(filePath, pageNumber, resources);
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
                float x = mark.getX() * originalBitmap.getWidth(),
                        y = mark.getY() * originalBitmap.getHeight();
                drawMark(mark, canvas, paint, x, y);
            }
            return currentBitmap;
        }
        return null;
    }

    private static void drawMark(Mark mark, Canvas canvas, Paint paint, float X, float Y) {
        String text = mark.getDevice().getRole();
        Rect bounds = new Rect();
        paint.getTextBounds(text,0, text.length(), bounds);

        float[] touchPoint = new float[]{X, Y};
        float tW = bounds.width(),
                tH = bounds.height();

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text over picture
        canvas.drawCircle(touchPoint[0], touchPoint[1], radius, circlePaint);
        canvas.drawText(text, touchPoint[0] - tW / 2, touchPoint[1] + tH / 2, paint);
    }
}
