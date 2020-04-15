package com.richard.weger.wqc.service;

import android.content.Context;

import com.richard.weger.wqc.util.App;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrScannerService {
    private Context context;
    private ZXingScannerView.ResultHandler handler;
    private ZXingScannerView mScannerView;

    public QrScannerService(ZXingScannerView.ResultHandler handler) {
        this.context = App.getContext();
        this.handler = handler;
    }

    public void pause() {
        if(mScannerView != null) {
            mScannerView.stopCamera();
        }
    }

    public void stop() {
        if(mScannerView != null) {
            mScannerView.stopCamera();
            mScannerView = null;
        }
    }

    public ZXingScannerView getContentView() {
        mScannerView = new ZXingScannerView(context);
        mScannerView.setResultHandler(handler);
        mScannerView.startCamera();
        return mScannerView;
    }

}
