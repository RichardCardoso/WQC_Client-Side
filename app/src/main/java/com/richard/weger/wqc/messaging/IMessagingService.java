package com.richard.weger.wqc.messaging;

public interface IMessagingService {

    void setup(IMessagingListener listener);
    void setListener(IMessagingListener listener, boolean callbackToListener);
    void removeListener();
}
