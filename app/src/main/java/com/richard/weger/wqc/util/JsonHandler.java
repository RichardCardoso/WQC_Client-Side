package com.richard.weger.wqc.util;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.richard.weger.wqc.adapter.DateTypeAdapter;
import com.richard.weger.wqc.adapter.ReportTypeAdapter;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;

import java.io.Serializable;
import java.util.Date;

import static com.richard.weger.wqc.util.LogHandler.writeData;

public class JsonHandler {
    private static Context context = App.getContext();
    public static String toJson(Serializable object){
        writeData("Saving project's json file.",context.getExternalFilesDir(null));
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        String output = gson.create().toJson(object);
        writeData("Project json file save completed.",context.getExternalFilesDir(null));
        return output;
    }

    public static Project toProject(String json) {
        Project project;

        GsonBuilder gson = new GsonBuilder();

        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        gson.registerTypeAdapter(Date.class, new DateTypeAdapter());

        try {
            project = gson.create().fromJson(json, Project.class);
        } catch (Exception Ex){
            return null;
        }
        if(project != null)
            writeData("Project json file load completed.",context.getExternalFilesDir(null));
        else
            writeData("Project json file load failed.",context.getExternalFilesDir(null));
        ProjectHandler.linkReferences(project);
        return project;
    }

    public static ParamConfigurations toParamConfigurations(String json){
        ParamConfigurations paramConfigurations;

        GsonBuilder gsonBuilder = new GsonBuilder();

        paramConfigurations = gsonBuilder.create().fromJson(json, ParamConfigurations.class);
        return paramConfigurations;
    }

    public static Mark toMark(String json){
        Mark mark;

        GsonBuilder gsonBuilder = new GsonBuilder();

        mark = gsonBuilder.create().fromJson(json, Mark.class);
        return mark;

    }

    public static Device toDevice(String json) {
        Device device;

        GsonBuilder gsonBuilder = new GsonBuilder();

        device = gsonBuilder.create().fromJson(json, Device.class);
        return device;
    }

}
