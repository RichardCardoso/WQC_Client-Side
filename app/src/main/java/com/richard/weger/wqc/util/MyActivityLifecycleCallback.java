package com.richard.weger.wqc.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class MyActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    private ActivityLifecycleCallbackHandler handler;

    public MyActivityLifecycleCallback(ActivityLifecycleCallbackHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(handler != null) {
            handler.activityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


    public interface ActivityLifecycleCallbackHandler {
        void activityResumed(Activity activity);
    }


}
