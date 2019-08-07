package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;

public interface IEntityRequestParametersResolver<T extends DomainEntity> {
    public EntityRestTemplateHelper<T> getEntity(T entity, EntityRestTemplateHelper.EntityRestResponse delegate);
    public EntityRestTemplateHelper<T> getEntities(T entity, EntityRestTemplateHelper.EntityRestResponse delegate);
    public EntityRestTemplateHelper<T> getEntitiesFromParent(T entity, EntityRestTemplateHelper.EntityRestResponse delegate);
    public EntityRestTemplateHelper<T> postEntity(T entity, EntityRestTemplateHelper.EntityRestResponse delegate);
    public EntityRestTemplateHelper<T> deleteEntity(T entity, EntityRestTemplateHelper.EntityRestResponse delegate);
}
