package com.richard.weger.wegerqualitycontrol.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.richard.weger.wegerqualitycontrol.domain.ControlCardReport;
import com.richard.weger.wegerqualitycontrol.domain.Report;

import java.lang.reflect.Type;

public class ReportTypeAdapter implements JsonSerializer<Report>, JsonDeserializer<Report> {

    @Override
    public Report deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        JsonElement element = jsonObject.get("properties");

        try {
            String thePackage = Report.class.getPackage().getName().concat(".");
            return context.deserialize(element, Class.forName(thePackage + type));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown element type: " + type, e);
        }
    }

    @Override
    public JsonElement serialize(Report src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(src.getClass().getSimpleName()));
        result.add("properties", context.serialize(src, src.getClass()));
        return result;
    }
}
