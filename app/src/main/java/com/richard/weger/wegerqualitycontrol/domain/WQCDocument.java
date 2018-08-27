package com.richard.weger.wegerqualitycontrol.domain;

import android.graphics.PointF;

import com.richard.weger.wegerqualitycontrol.util.FileHandler;
import com.richard.weger.wegerqualitycontrol.util.PdfHandler;
import com.richard.weger.wegerqualitycontrol.util.WQCPointF;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WQCDocument implements Serializable {

    private Map<Integer, List<WQCPointF>> hashPoints = new HashMap<>();
    private String originalFileLocalPath = "";

    public int getMarksCount(){
        int cnt = 0;
        for(int i = 0; i < getHashPoints().size(); i++){
            if(getHashPoints().get(i) != null){
                for(WQCPointF p : getHashPoints().get(i)){
                    cnt ++;
                }
            }
        }
        return cnt;
    }

    public Map<Integer, List<WQCPointF>> getHashPoints() {
        return hashPoints;
    }

    public void setHashPoints(Map<Integer, List<WQCPointF>> hashPoints) {
        this.hashPoints = hashPoints;
    }

    public String getOriginalFileLocalPath() {
        return originalFileLocalPath;
    }

    public void setOriginalFileLocalPath(String originalFileLocalPath) {
        this.originalFileLocalPath = originalFileLocalPath;
    }

    public int getPageCount(){
        return PdfHandler.getPageCount(getOriginalFileLocalPath());
    }

    public boolean isDocumentReady(){
        if(FileHandler.isValidFile(getOriginalFileLocalPath()) &&
                getPageCount() > 0 && getMarksCount() > 0){
            return true;
        }
        return false;
    }
}
