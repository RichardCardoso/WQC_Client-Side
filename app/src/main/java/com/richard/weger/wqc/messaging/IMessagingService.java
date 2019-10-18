package com.richard.weger.wqc.messaging;

public interface IMessagingService {

    void setListener(IMessagingListener listener, boolean callbackToListener);
    void removeListener();
}
