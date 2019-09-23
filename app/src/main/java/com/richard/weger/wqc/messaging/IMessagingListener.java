package com.richard.weger.wqc.messaging;

public interface IMessagingListener {
    boolean shouldNotifyChange(String qrCode, Long entityId, Long parentId);
    void onConnectionSuccess();
    void onConnectionFailure();
}
