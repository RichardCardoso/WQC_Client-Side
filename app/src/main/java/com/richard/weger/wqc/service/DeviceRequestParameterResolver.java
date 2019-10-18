package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.rest.entity.RawEntityRequest;

public class DeviceRequestParameterResolver extends AbstractEntityRequestParametersResolver<Device> {

    public DeviceRequestParameterResolver(String requestCode, ParamConfigurations conf, boolean toggleControlsOnCompletion) {
        super(requestCode, conf, toggleControlsOnCompletion);
    }

    @Override
    protected void entityGetStrategy(Device entity, RawEntityRequest<Device> request) {
        request.setOverriddenResource("/devices/" + entity.getDeviceid());
    }

    @Override
    protected void entitiesGetStrategy(Device entity, RawEntityRequest<Device> request) {

    }

    @Override
    protected void entitiesGetFromParentStrategy(Device entity, RawEntityRequest<Device> request) {

    }

    @Override
    protected void entityPostStrategy(Device entity, RawEntityRequest<Device> request) {

    }

    @Override
    protected void entityDeleteStrategy(Device entity, RawEntityRequest<Device> request) {

    }

}
