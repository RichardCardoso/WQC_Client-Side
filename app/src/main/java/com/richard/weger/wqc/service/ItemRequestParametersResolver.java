package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.rest.entity.RawEntityRequest;

public class ItemRequestParametersResolver extends AbstractEntityRequestParametersResolver<Item> {

    public ItemRequestParametersResolver(String requestCode, ParamConfigurations conf, boolean toggleControlsOnCompletion) {
        super(requestCode, conf, toggleControlsOnCompletion);
    }

    @Override
    protected void entityGetStrategy(Item entity, RawEntityRequest<Item> request) {

    }

    @Override
    protected void entitiesGetStrategy(Item entity, RawEntityRequest<Item> request) {

    }

    @Override
    protected void entitiesGetFromParentStrategy(Item entity, RawEntityRequest<Item> request) {

    }

    @Override
    protected void entityPostStrategy(Item entity, RawEntityRequest<Item> request) {

    }

    @Override
    protected void entityDeleteStrategy(Item entity, RawEntityRequest<Item> request) {

    }

}
