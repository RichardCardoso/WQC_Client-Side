package com.richard.weger.wqc.messaging.websocket;

import com.richard.weger.wqc.messaging.IMessagingListener;
import com.richard.weger.wqc.messaging.IMessagingService;
import com.richard.weger.wqc.messaging.MessagingHelper;
import com.richard.weger.wqc.service.AsyncMethodExecutor;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.LoggerManager;

import java.net.URI;
import java.util.Locale;

import static com.richard.weger.wqc.util.App.getLocale;

public class WebsocketService implements IMessagingService {

    private WebsocketClient socket;

    private void setup(IMessagingListener listener) {
        AsyncMethodExecutor.execute(() -> {
            try {
                LoggerManager.getLogger(WebsocketService.class).info("Websocket setup event was called");
                Configurations conf = ConfigurationsManager.getLocalConfig();
                Locale l = getLocale();
                String pathWithPort = conf.getServerPath();
                URI uri = new URI(String.format(l, "ws://%s/wqc", pathWithPort));
                socket = new WebsocketClient(uri, listener);
                socket.setConnectTimeout(10000);
                socket.setReadTimeout(60000);
                socket.enableAutomaticReconnection(5000);
                socket.connect();
                LoggerManager.getLogger(WebsocketService.class).info("Successfully connected to the messaging endpoint");
            } catch (Exception ex) {
                MessagingHelper.failureHandler(ex, true);
            }
        });
    }

    @Override
    public void setListener(IMessagingListener listener, boolean callbackToListener) {
        LoggerManager.getLogger(WebsocketService.class).info("Setting messaging listener");
        if(socket == null || !socket.firstConnectionDone) {
            setup(listener);
        } else {
            socket.setListener(listener);
            if (callbackToListener && listener != null) {
                listener.onConnectionSuccess();
            }
        }
    }

    @Override
    public void removeListener() {
        LoggerManager.getLogger(WebsocketService.class).info("Removing messaging listener");
        socket.removeListener();
    }

}
