package com.richard.weger.wegerqualitycontrol.domain;

import android.graphics.PointF;

import com.richard.weger.wegerqualitycontrol.util.WQCPointF;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WQCDocument implements Serializable {
    private Map<Integer, List<WQCPointF>> hashPoints = new HashMap<>();

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
}
