package com.richard.weger.wegerqualitycontrol.domain;

import android.graphics.Bitmap;

public class ElectricReport extends Report {
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "Electric Report";
    }

    @Override
    protected void fillItemsList() {

    }
}
