package com.richard.weger.wqc.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.richard.weger.wqc.adapter.ReportTypeAdapter;
import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.domain.Report;

import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.util.Date;

public class MyHttpMessageConverter extends GsonHttpMessageConverter {

    public MyHttpMessageConverter(){
        setGson(buildGson());
    }

    private static Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat(AppConstants.SDF.toPattern());
        builder.registerTypeAdapter(Date.class, new MyJsonDateSerializer());
        builder.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        builder.addSerializationExclusionStrategy(new ReportItemExclusionStrategy());
        return builder.create();
    }
}
