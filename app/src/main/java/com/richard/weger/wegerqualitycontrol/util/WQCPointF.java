package com.richard.weger.wegerqualitycontrol.util;

import java.io.Serializable;

public class WQCPointF implements Serializable {
    private float x;
    private float y;

    public WQCPointF(float x, float y){
        setX(x);
        setY(y);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
