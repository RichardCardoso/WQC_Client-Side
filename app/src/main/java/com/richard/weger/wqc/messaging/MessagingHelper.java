package com.richard.weger.wqc.messaging;

import android.content.res.Resources;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.messaging.firebird.FirebaseHelper;
import com.richard.weger.wqc.messaging.websocket.MessagingDTO;
import com.richard.weger.wqc.messaging.websocket.WebsocketService;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MessagingHelper {

    private static IMessagingService INSTANCE = null;

    public static IMessagingService getServiceInstance(){
        if(INSTANCE == null){
            synchronized (MessagingHelper.class){
                if(INSTANCE == null) {
                    INSTANCE = new WebsocketService();
                }
            }
        }
        return INSTANCE;
    }

    public static ErrorResult getError(){
        Resources r;
        r = App.getContext().getResources();
        return new ErrorResult(ErrorResult.ErrorCode.CLIENT_UPDATE_SERVICE_CONNECTION_FAILED,
                r.getString(R.string.pushMessageConnectionFailed), ErrorResult.ErrorLevel.SEVERE, FirebaseHelper.class);
    }

    public static void failureHandler(Exception e, boolean subscribeError){
        if(subscribeError){
            ErrorResult err = getError();

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String ex = sw.toString();

            LoggerManager.getLogger(FirebaseHelper.class).severe(ex);
            ErrorResponseHandler.handle(err, App.getContext(), () -> FirebaseHelper.delegate.onConnectionFailure());
        } else {
            LoggerManager.getLogger(FirebaseHelper.class).warning("Failed to unsubscribe from Firebird service. Maybe subscription was not performed yet.");
        }

    }

    public static void handleData(MessagingDTO dto, IMessagingListener delegate){
        if (delegate != null) {
            String qrCode = dto.getQrcode();
            if (qrCode != null && qrCode.equals(ProjectHelper.getQrCode())) {
                LoggerManager.getLogger(FirebaseHelper.class).info("Qr code matches with opened project.");
                try {
                    long id = -1L;
                    long parentId = -1L;
                    if (dto.getId() != null) {
                        id = dto.getId();
                    }
                    if (dto.getParentId() != null) {
                        parentId = dto.getParentId();
                    }
                    boolean shouldNotify = delegate.shouldNotifyChange(qrCode, id, parentId);
                    if (shouldNotify) {
                        Resources r;
                        r = App.getContext().getResources();
                        AlertHelper.showNotification(r.getString(R.string.projectUpdatedMessage), qrCode);
                    }
                } catch (Exception ex) {
                    LoggerManager.log(MessagingHelper.class, "Error during push message id parse!\n" + ex.getMessage(), ErrorResult.ErrorLevel.SEVERE);
                }

            } else {
                LoggerManager.getLogger(FirebaseHelper.class).info("Qr code does not match with opened project.");
            }
        }
    }

}
