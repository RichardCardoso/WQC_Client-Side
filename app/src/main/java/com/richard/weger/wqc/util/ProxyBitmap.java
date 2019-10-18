package com.richard.weger.wqc.util;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ProxyBitmap implements Serializable{
    private int[] pixels;
    private int width, height;
    private boolean initialized = false;

    public ProxyBitmap(Bitmap bitmap){
        if(bitmap == null) {
            return;
        }
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        initialized = true;
    }

    public Bitmap getBitmap(){
        if(initialized)
            return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        else
            return null;
    }
}
