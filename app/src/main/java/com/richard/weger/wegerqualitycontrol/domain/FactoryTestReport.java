package com.richard.weger.wegerqualitycontrol.domain;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class FactoryTestReport extends Report {

    public FactoryTestReport(){
        setBitmap(null);
    }

    @Override
    public String toString() {
        return "Factory Test Report";
    }

    @Override
    protected void fillItemsList() {

    }

    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
