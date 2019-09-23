package com.richard.weger.wqc.messaging.websocket;

import com.google.gson.Gson;
import com.richard.weger.wqc.messaging.IMessagingListener;
import com.richard.weger.wqc.messaging.MessagingHelper;
import com.richard.weger.wqc.messaging.firebird.FirebaseHelper;
import com.richard.weger.wqc.util.LoggerManager;

import java.net.URI;

import tech.gusavila92.websocketclient.WebSocketClient;

public class WebsocketClient extends WebSocketClient {

    private IMessagingListener delegate;

    /**
     * Initialize all the variables
     *
     * @param uri URI of the WebSocket server
     */
    WebsocketClient(URI uri, IMessagingListener delegate) {
        super(uri);
        this.delegate = delegate;
    }

    @Override
    public void onOpen() {
        delegate.onConnectionSuccess();
    }

    @Override
    public void onTextReceived(String message) {
        if (message.length() > 0) {
            LoggerManager.getLogger(FirebaseHelper.class).info( "Message data payload: " + message);
            MessagingDTO dto;
            Gson gson = new Gson();
            dto = gson.fromJson(message, MessagingDTO.class);
            MessagingHelper.handleData(dto, delegate);
        } else {
            LoggerManager.getLogger(FirebaseHelper.class).info( "Empty message payload was received!");
        }
    }

    @Override
    public void onBinaryReceived(byte[] data) {

    }

    @Override
    public void onPingReceived(byte[] data) {

    }

    @Override
    public void onPongReceived(byte[] data) {

    }

    @Override
    public void onException(Exception e) {
        MessagingHelper.failureHandler(e, true);
    }

    @Override
    public void onCloseReceived() {

    }


    void setListener(IMessagingListener listener) {
        delegate = listener;
    }

    void removeListener() {
        delegate = null;
    }

}
