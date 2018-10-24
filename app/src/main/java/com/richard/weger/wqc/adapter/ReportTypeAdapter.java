package com.richard.weger.wqc.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.Report;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReportTypeAdapter implements JsonSerializer<Report>, JsonDeserializer<Report> {

    @Override
    public Report deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        try {
            String className = Report.class.getPackage().getName().concat(".").concat(type);
            Report report = context.deserialize(jsonObject, Class.forName(className));

            switch(type){
                case "CheckReport":
                    final JsonArray jsonPagesArray = jsonObject.get("pages").getAsJsonArray();
                    final List<Page> pageList = new ArrayList<>();
                    for(int i = 0; i < jsonPagesArray.size(); i++){
                        final JsonElement jsonPage = jsonPagesArray.get(i);
                        Page page = context.deserialize(jsonPage, Page.class);

                        final JsonArray jsonMarksArray = ((JsonObject)jsonPage).get("marks").getAsJsonArray();
                        final List<Mark> markList = new ArrayList<>();
                        for(int j = 0; j < jsonMarksArray.size(); j++){
                            final JsonElement jsonMark = jsonMarksArray.get(j);
                            Mark mark = context.deserialize(jsonMark, Mark.class);
                            markList.add(mark);
                        }
                        page.setMarks(markList);
                        pageList.add(page);
                    }
                    ((CheckReport)report).setPages(pageList);
                    break;
                case "ItemReport":
                    final List<Item> itemList = new ArrayList<>();
                    final JsonArray jsonItemsArray;
                    JsonElement jsonElement = jsonObject.get("items");
                    if(jsonElement != null) {
                        jsonItemsArray = jsonElement.getAsJsonArray();
                        for (int i = 0; i < jsonItemsArray.size(); i++) {
                            final JsonElement jsonItem = jsonItemsArray.get(i);
                            itemList.add((Item) context.deserialize(jsonItem, Item.class));
                        }
                        ((ItemReport) report).setItems(itemList);
                    }
            }

            return report;
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown element type: " + type, e);
        }
    }

    @Override
    public JsonElement serialize(Report src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src, src.getClass());
    }
}
