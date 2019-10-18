package com.richard.weger.wqc.helper;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.messaging.IMessagingListener;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.util.App;

import java.util.Arrays;
import java.util.List;

public class ActivityHelper {

    public static void setWaitingLayout(Activity target){
        setWaitingLayout(target,null);
    }

    public static void setHandlerWaitingLayout(RestTemplateHelper.RestResponseHandler handler, String message){
        if(handler instanceof Activity){
            Activity t = (Activity) handler;
            t.runOnUiThread(() -> setWaitingLayout(t, message));
        }
    }

    private static void setWaitingLayout(Activity target, String message){
        target.setContentView(R.layout.activity_wait);
        (target.findViewById(R.id.pbWelcome)).setVisibility(View.VISIBLE);
        TextView tvStatus = (target.findViewById(R.id.tvMessage));

        target.runOnUiThread(() -> {
            ImageButton btnExit;
            btnExit = target.findViewById(R.id.btnExit);
            //                        android.os.Process.killProcess(android.os.Process.myPid());
            btnExit.setOnClickListener((View v) -> AlertHelper.showMessage(
                    target,
                    target.getResources().getString(R.string.confirmationNeeded),
                    target.getResources().getString(R.string.cancelQuestion),
                    target.getResources().getString(R.string.yesTAG),
                    target.getResources().getString(R.string.noTag),
//                        android.os.Process.killProcess(android.os.Process.myPid());
                    target::finish,
                    null)
            );
        });

        if(message != null){
            tvStatus.setText(message);
        } else {
            tvStatus.setText(R.string.mustWaitCompletion);
        }
    }

    public static void disableHandlerControls(RestTemplateHelper.RestResponseHandler target, boolean allowCancel){
        if(target instanceof Activity) {
            Activity t = (Activity) target;
            t.runOnUiThread(() -> disableActivityControls(t, allowCancel));
        }
    }

    public static void disableActivityControls(Activity target, boolean allowCancel){
        List<Integer> cancelControls = Arrays.asList(R.id.backButton, R.id.backButton, R.id.btnExit);
        ViewGroup g = (ViewGroup) target.getWindow().getDecorView();
        for (int i = 0; i < g.getChildCount(); i++) {
            View child = g.getChildAt(i);
            if (!allowCancel || !cancelControls.contains(child.getId())) {
                target.runOnUiThread(() -> child.setEnabled(false));
            }
        }
    }

    public static void activityDisable(IMessagingListener listener){
        if (listener instanceof Activity){
            Activity t;
            Resources r;
            String message;

            t = (Activity) listener;
            r = App.getContext().getResources();
            message = String.format(r.getConfiguration().getLocales().get(0), "%s, %s",
                    r.getString(R.string.messagingServiceConnectingMessage), r.getString(R.string.pleaseWaitMessage).toLowerCase());

            setWaitingLayout(t, message);
        }
    }

}
