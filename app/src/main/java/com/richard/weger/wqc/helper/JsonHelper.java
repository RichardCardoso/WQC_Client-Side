package com.richard.weger.wqc.helper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.richard.weger.wqc.adapter.DateTypeAdapter;
import com.richard.weger.wqc.adapter.ReportTypeAdapter;
import com.richard.weger.wqc.domain.Report;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JsonHelper {

    public static <T> List<T> toList(String jsonData, Class<T> klass){
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(jsonData).getAsJsonArray();

        GsonBuilder gsonBuilder = new GsonBuilder();

        List<T> list = new ArrayList<T>();

        for(final JsonElement json : array){
            T entity = gsonBuilder.create().fromJson(json, klass);
            list.add(entity);
        }

        return list;
    }

    public static <T> T toObject(String json, Class<T> klass){
        Object object;

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(Report.class, new ReportTypeAdapter());
        gsonBuilder.registerTypeAdapter(Date.class, new DateTypeAdapter());

        object = gsonBuilder.create().fromJson(json, klass);
        return klass.cast(object);
    }
}
