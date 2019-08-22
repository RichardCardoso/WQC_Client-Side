package com.richard.weger.wqc.converter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.util.LoggerManager;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

public class MyJsonDateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {

    public MyJsonDateSerializer(){
        logger = LoggerManager.getLogger(getClass());
    }

    private Logger logger;

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        String sDate = AppConstants.SDF.format(src);
        return context.serialize(sDate);
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonPrimitive object = (JsonPrimitive) json;
        String sDate = object.getAsString();
        try {
            return AppConstants.SDF.parse(sDate);
        } catch (ParseException e) {
            logger.warning(e.toString());
            return null;
        }
    }
}
