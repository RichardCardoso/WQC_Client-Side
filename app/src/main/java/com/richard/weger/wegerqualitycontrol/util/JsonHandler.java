package com.richard.weger.wegerqualitycontrol.util;

import android.app.Activity;
import android.content.Context;

import com.google.gson.GsonBuilder;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;

import java.io.Serializable;

import static com.richard.weger.wegerqualitycontrol.util.LogHandler.writeData;

public class JsonHandler {
    public static boolean jsonProjectSave(Context context, String fileName, Serializable object){
        writeData("Saving project's json file.",context.getExternalFilesDir(null));
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        String output = gson.create().toJson(object);
        writeData("Project json file save completed.",context.getExternalFilesDir(null));
        return FileHandler.localFileSave(context, fileName, output);
    }

    public static Project jsonProjectLoad(Context context, String fileName) {
        String input;
        Project project;

        writeData("Loading project's json file.",context.getExternalFilesDir(null));
        GsonBuilder gson = new GsonBuilder();

        input = FileHandler.localFileLoadAsString(context, fileName);
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        project = gson.create().fromJson(input, Project.class);
        if(project != null)
            writeData("Project json file load completed.",context.getExternalFilesDir(null));
        else
            writeData("Project json file load failed.",context.getExternalFilesDir(null));
        return project;
    }
}
