package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;

public interface IEntityRequestParametersResolver<T extends DomainEntity> {
    EntityRestTemplateHelper<T> getEntity(T entity, RestTemplateHelper.RestResponseHandler delegate);
    EntityRestTemplateHelper<T> getEntities(T entity, RestTemplateHelper.RestResponseHandler delegate);
    EntityRestTemplateHelper<T> getEntitiesFromParent(T entity, RestTemplateHelper.RestResponseHandler delegate);
    EntityRestTemplateHelper<T> postEntity(T entity, RestTemplateHelper.RestResponseHandler delegate);
    EntityRestTemplateHelper<T> deleteEntity(T entity, RestTemplateHelper.RestResponseHandler delegate);
}
