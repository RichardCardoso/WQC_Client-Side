package com.richard.weger.wqc.adapter;

public interface ReportItemActionHandler {
    void onPictureTap(int position);
    void onCommentsChange(int position, String newContent);
    void onStatusTap(int value, int position);
    void onRequestPictureCapture(int position);
}
