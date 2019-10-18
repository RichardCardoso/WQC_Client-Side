package com.richard.weger.wqc.service;

import android.os.AsyncTask;

import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.util.LoggerManager;
import com.richard.weger.wqc.util.Method;

public class AsyncMethodExecutor extends AsyncTask<Method, Void, Boolean> {

    public PostExecutionListener getListener() {
        return listener;
    }

    public void setListener(PostExecutionListener listener) {
        this.listener = listener;
    }

    public interface PostExecutionListener {
        void onPostExecute(Boolean result);
    }

    private PostExecutionListener listener;

    @Override
    protected Boolean doInBackground(Method... methods) {
        try {
            Method m;
            m = methods[0];
            m.execute();
            return true;
        } catch (Exception e) {
            String ex = StringHelper.getStackTraceAsString(e);
            LoggerManager.getLogger(AsyncMethodExecutor.class).severe(ex);
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean result){
        if(listener != null){
            listener.onPostExecute(result);
        }
    }

}
