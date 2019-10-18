package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.helper.DeviceHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.entity.EntityRequest;
import com.richard.weger.wqc.rest.entity.EntityRequestHelper;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.rest.entity.EntityReturnType;
import com.richard.weger.wqc.rest.entity.RawEntityRequest;

import java.util.Date;

import static com.richard.weger.wqc.appconstants.AppConstants.DELETE_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.GET_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.POST_METHOD;

public abstract class AbstractEntityRequestParametersResolver<T extends AuditableEntity> implements IEntityRequestParametersResolver<T> {

    private String requestCode;
    private ParamConfigurations conf;
    private boolean toggleControlsOnCompletion;

    AbstractEntityRequestParametersResolver(String requestCode, ParamConfigurations conf, boolean toggleControlsOnCompletion){
        this.requestCode = requestCode;
        this.conf = conf;
        this.toggleControlsOnCompletion = toggleControlsOnCompletion;
    }

    @Override
    public final EntityRestTemplateHelper<T> getEntity(T entity, RestTemplateHelper.RestResponseHandler delegate) {
        EntityRestTemplateHelper<T> template = new EntityRestTemplateHelper<>(delegate, toggleControlsOnCompletion);

        if(entity == null){
            return null;
        }

        RawEntityRequest<T> request = new RawEntityRequest<>(entity);
        request.setRequestMethod(GET_METHOD);
        request.setRequestCode(requestCode);
        request.setEntityReturnType(EntityReturnType.SingleEntityReturn);

        entityGetStrategy(entity, request);

        EntityRequestHelper helper = new EntityRequestHelper();
        EntityRequest<T> req = helper.proccess(request, null);

        template.execute(req);

        return template;
    }

    @Override
    public final EntityRestTemplateHelper<T> getEntities(T entity, RestTemplateHelper.RestResponseHandler delegate) {
        EntityRestTemplateHelper<T> template = new EntityRestTemplateHelper<>(delegate, toggleControlsOnCompletion);

        if(entity == null){
            return null;
        }

        RawEntityRequest<T> request = new RawEntityRequest<>(entity);
        request.setRequestMethod(GET_METHOD);
        request.setRequestCode(requestCode);
        request.setEntityReturnType(EntityReturnType.EntityListReturn);

        entitiesGetStrategy(entity, request);

        EntityRequestHelper helper = new EntityRequestHelper();
        EntityRequest<T> req = helper.proccess(request, null);

        template.execute(req);

        return template;
    }

    @Override
    public EntityRestTemplateHelper<T> getEntitiesFromParent(T entity, RestTemplateHelper.RestResponseHandler delegate) {
        EntityRestTemplateHelper<T> template = new EntityRestTemplateHelper<>(delegate, toggleControlsOnCompletion);

        if(!(entity instanceof ParentAwareEntity) || ((ParentAwareEntity) entity).getParent() == null){
            return null;
        }

        RawEntityRequest<T> request = new RawEntityRequest<>(entity);
        request.setRequestMethod(GET_METHOD);
        request.setRequestCode(requestCode);
        request.setEntityReturnType(EntityReturnType.EntityListReturn);

        RequestParameter param = new RequestParameter();
        param.setName("parentid");
        param.setValue(String.valueOf(((ParentAwareEntity)entity).getParent().getId()));
        request.getParameters().add(param);

        entitiesGetFromParentStrategy(entity, request);

        EntityRequestHelper helper = new EntityRequestHelper();
        EntityRequest<T> req = helper.proccess(request, null);

        template.execute(req);

        return template;
    }

    @Override
    public final EntityRestTemplateHelper<T> postEntity(T entity, RestTemplateHelper.RestResponseHandler delegate) {
        EntityRestTemplateHelper<T> template = new EntityRestTemplateHelper<>(delegate, toggleControlsOnCompletion);

        RawEntityRequest<T> request = new RawEntityRequest<>(entity);
        request.setRequestMethod(POST_METHOD);
        request.setRequestCode(requestCode);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(ProjectHelper.getQrCode());
        request.getParameters().add(param);

        Device dev = DeviceHelper.getCurrentDevice();
        entity.setLastModifiedBy(dev.getName() + " (" + dev.getDeviceid() + ")");
        entity.setLastModifiedDate(new Date());

        entityPostStrategy(entity, request);

        EntityRequestHelper helper = new EntityRequestHelper();
        EntityRequest<T> req = helper.proccess(request, null);

        template.execute(req);

        return template;
    }

    @Override
    public EntityRestTemplateHelper<T> deleteEntity(T entity, RestTemplateHelper.RestResponseHandler delegate) {
        EntityRestTemplateHelper<T> template = new EntityRestTemplateHelper<>(delegate, toggleControlsOnCompletion);

        RawEntityRequest<T> request = new RawEntityRequest<>(entity);
        request.setRequestMethod(DELETE_METHOD);
        request.setRequestCode(requestCode);

        Device dev = DeviceHelper.getCurrentDevice();
        entity.setLastModifiedBy(dev.getName() + " (" + dev.getDeviceid() + ")");

        entityDeleteStrategy(entity, request);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(ProjectHelper.getQrCode());
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("id");
        param.setValue(String.valueOf(entity.getId()));
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("version");
        param.setValue(String.valueOf(entity.getVersion()));
        request.getParameters().add(param);

        EntityRequestHelper helper = new EntityRequestHelper();
        EntityRequest<T> req = helper.proccess(request, null);

        template.execute(req);

        return template;
    }

    protected abstract void entityGetStrategy(T entity, RawEntityRequest<T> request);

    protected abstract void entitiesGetStrategy(T entity, RawEntityRequest<T> request);

    protected abstract void entitiesGetFromParentStrategy(T entity, RawEntityRequest<T> request);

    protected abstract void entityPostStrategy(T entity, RawEntityRequest<T> request);

    protected abstract void entityDeleteStrategy(T entity, RawEntityRequest<T> request);

    public ParamConfigurations getConf() {
        return conf;
    }
}
