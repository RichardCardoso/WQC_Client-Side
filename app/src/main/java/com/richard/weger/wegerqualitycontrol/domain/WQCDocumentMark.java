package com.richard.weger.wegerqualitycontrol.domain;

import com.richard.weger.wegerqualitycontrol.util.WQCPointF;

import java.io.Serializable;

public class WQCDocumentMark implements Serializable {
    private WQCPointF pointF;
    /**
     * type = 0: ok mark
     * type = 1: el mark
     * type = 2: mag mark
     */
    private int type = 0;

    public WQCDocumentMark(WQCPointF wqcPointF, int markType){
        setPointF(wqcPointF);
        setType(markType);
    }

    public WQCPointF getPointF() {
        return pointF;
    }

    public void setPointF(WQCPointF pointF) {
        this.pointF = pointF;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
