package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.entity.RawEntityRequest;

public class MarkRequestParametersResolver extends AbstractEntityRequestParametersResolver<Mark> {

    public MarkRequestParametersResolver(String requestCode, ParamConfigurations conf, boolean toggleControlsOnCompletion) {
        super(requestCode, conf, toggleControlsOnCompletion);
    }

    @Override
    protected void entityGetStrategy(Mark entity, RawEntityRequest<Mark> request) {

    }

    @Override
    protected void entitiesGetStrategy(Mark entity, RawEntityRequest<Mark> request) {

    }

    @Override
    protected void entitiesGetFromParentStrategy(Mark entity, RawEntityRequest<Mark> request) {

    }

    @Override
    protected void entityPostStrategy(Mark entity, RawEntityRequest<Mark> request) {
        RequestParameter param = new RequestParameter();
        param.setName("parentid");
        param.setValue(String.valueOf(entity.getParent().getId()));
        request.getParameters().add(param);
    }

    @Override
    protected void entityDeleteStrategy(Mark entity, RawEntityRequest<Mark> request) {

    }

}
