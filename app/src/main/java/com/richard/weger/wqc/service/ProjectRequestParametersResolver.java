package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.entity.RawEntityRequest;

import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTCREATE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;

public class ProjectRequestParametersResolver extends AbstractEntityRequestParametersResolver<Project> {


    public ProjectRequestParametersResolver(String requestCode, ParamConfigurations conf, boolean toggleControlsOnCompletion) {
        super(requestCode, conf, toggleControlsOnCompletion);
    }

    @Override
    protected void entityGetStrategy(Project entity, RawEntityRequest<Project> request) {
        if(request.getRequestCode().equals(REST_QRPROJECTLOAD_KEY) && request.getParameters().stream().noneMatch(p -> p.getName().equals("qrcode"))) {
            RequestParameter param = new RequestParameter();
            param.setName("qrcode");
            param.setValue(StringHelper.getQrText(entity));
            request.getParameters().add(param);
        }
        request.setOverriddenResource("/qrcode/projects");
    }

    @Override
    protected void entitiesGetStrategy(Project entity, RawEntityRequest<Project> request) {

    }

    @Override
    protected void entitiesGetFromParentStrategy(Project entity, RawEntityRequest<Project> request) {

    }

    @Override
    protected void entityPostStrategy(Project entity, RawEntityRequest<Project> request) {
        if(request.getRequestCode().equals(REST_QRPROJECTCREATE_KEY) && request.getParameters().stream().noneMatch(p -> p.getName().equals("qrcode"))) {
            RequestParameter param = new RequestParameter();
            param.setName("qrcode");
            param.setValue(StringHelper.getQrText(entity));
            request.getParameters().add(param);
        }
        request.setOverriddenResource("/qrcode/projects");
    }

    @Override
    protected void entityDeleteStrategy(Project entity, RawEntityRequest<Project> request) {

    }

}
