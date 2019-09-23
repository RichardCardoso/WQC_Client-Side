package com.richard.weger.wqc.service;

import android.os.AsyncTask;

import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AsyncMethodExecutor extends AsyncTask<App.Method, Void, Boolean> {

    @Override
    protected Boolean doInBackground(App.Method... methods) {
        try {
            App.Method m;
            m = methods[0];
            m.execute();
            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String ex = sw.toString();
            LoggerManager.getLogger(AsyncMethodExecutor.class).severe(ex);
            return false;
        }
    }
}
