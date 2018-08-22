package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;

import java.io.Serializable;

public class JsonHandler {
    public static boolean jsonProjectSave(Context context, String fileName, Serializable object){
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        String output = gson.create().toJson(object);
        return FileHandler.localFileSave(context, fileName, output);
    }

    public static Project jsonProjectLoad(Context context, String fileName) {
        String input;
        Project project;
        GsonBuilder gson = new GsonBuilder();

        input = FileHandler.localFileLoadAsString(context, fileName);
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        project = gson.create().fromJson(input, Project.class);
        return project;
    }
}
