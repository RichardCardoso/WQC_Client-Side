package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.rest.entity.RawEntityRequest;


public class ParamConfigurationsRequestParametersResolver extends AbstractEntityRequestParametersResolver<ParamConfigurations> {


    public ParamConfigurationsRequestParametersResolver(String requestCode, ParamConfigurations conf, boolean toggleControlsOnCompletion) {
        super(requestCode, conf, toggleControlsOnCompletion);
    }

    @Override
    protected void entityGetStrategy(ParamConfigurations entity, RawEntityRequest<ParamConfigurations> request) {
        entity.setId(1L);
    }

    @Override
    protected void entitiesGetStrategy(ParamConfigurations entity, RawEntityRequest<ParamConfigurations> request) {

    }

    @Override
    protected void entitiesGetFromParentStrategy(ParamConfigurations entity, RawEntityRequest<ParamConfigurations> request) {

    }

    @Override
    protected void entityPostStrategy(ParamConfigurations entity, RawEntityRequest<ParamConfigurations> request) {

    }

    @Override
    protected void entityDeleteStrategy(ParamConfigurations entity, RawEntityRequest<ParamConfigurations> request) {

    }
}
