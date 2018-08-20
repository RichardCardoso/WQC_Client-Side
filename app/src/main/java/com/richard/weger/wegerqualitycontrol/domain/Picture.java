package com.richard.weger.wegerqualitycontrol.domain;

import com.richard.weger.wegerqualitycontrol.util.ProxyBitmap;

import java.io.Serializable;

public class Picture implements Serializable{
    public Picture(){
        proxyBitmap = new ProxyBitmap(null);
    }
    private transient ProxyBitmap proxyBitmap;
    private String filePath;

    public ProxyBitmap getProxyBitmap() {
        return proxyBitmap;
    }

    public void setProxyBitmap(ProxyBitmap proxyBitmap) {
        this.proxyBitmap = proxyBitmap;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
