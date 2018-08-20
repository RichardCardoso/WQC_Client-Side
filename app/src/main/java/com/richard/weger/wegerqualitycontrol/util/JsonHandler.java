package com.richard.weger.wegerqualitycontrol.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.richard.weger.wegerqualitycontrol.domain.ControlCardReport;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

public class JsonHandler {
    public static boolean jsonProjectSave(Context context, String fileName, Serializable object){
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        String output = gson.create().toJson(object);
        return FileHandler.fileSave(context, fileName, output);
    }

    public static Project jsonProjectLoad(Context context, String fileName){
        String input;
        Project project;
        GsonBuilder gson = new GsonBuilder();

        input = FileHandler.fileLoadAsString(context, fileName);
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        project = gson.create().fromJson(input, Project.class);
        return project;
    }

    public static boolean jsonConfigSave(Context context, String fileName, Serializable object){
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        String output = gson.create().toJson(object);
        return FileHandler.fileSave(context, fileName, output);
    }

    public static ConfigurationsManager jsonConfigLoad(Context context, String fileName){
        String input;
        ConfigurationsManager project;
        GsonBuilder gson = new GsonBuilder();

        input = FileHandler.fileLoadAsString(context, fileName);
        gson.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        project = gson.create().fromJson(input, ConfigurationsManager.class);
        return project;
    }

    public String toJson(Context context, Serializable object){
        Gson gson = new Gson();
        String output = gson.toJson(object);
        return output;
    }
}
