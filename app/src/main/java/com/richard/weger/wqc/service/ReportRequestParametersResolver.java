package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.rest.entity.RawEntityRequest;

public class ReportRequestParametersResolver extends AbstractEntityRequestParametersResolver<Report> {

    public ReportRequestParametersResolver(String requestCode, ParamConfigurations conf, boolean toggleControlsOnCompletion) {
        super(requestCode, conf, toggleControlsOnCompletion);
    }

    @Override
    protected void entityGetStrategy(Report entity, RawEntityRequest<Report> request) {

    }

    @Override
    protected void entitiesGetStrategy(Report entity, RawEntityRequest<Report> request) {

    }

    @Override
    protected void entitiesGetFromParentStrategy(Report entity, RawEntityRequest<Report> request) {

    }

    @Override
    protected void entityPostStrategy(Report entity, RawEntityRequest<Report> request) {
//        if(entity instanceof ItemReport){
//            ItemReport r = (ItemReport) entity;
//
//            String json;
//            GsonBuilder builder = new GsonBuilder();
//            builder.setDateFormat(SDF.toPattern());
//            builder.registerTypeAdapter(Report.class, new ReportTypeAdapter());
//            builder.registerTypeAdapter(Date.class, new MyJsonDateSerializer());
//            json = builder.create().toJson(r);
//            r = builder.create().fromJson(json, ItemReport.class);
//            r.setChildren(null);
//
//            request.setSingleEntity(r);
//        }
    }

    @Override
    protected void entityDeleteStrategy(Report entity, RawEntityRequest<Report> request) {

    }

}
