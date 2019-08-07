package com.richard.weger.wqc.converter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.richard.weger.wqc.domain.ItemReport;

public class ReportItemExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaringClass() == ItemReport.class && f.getName().toLowerCase().equals("items");
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
