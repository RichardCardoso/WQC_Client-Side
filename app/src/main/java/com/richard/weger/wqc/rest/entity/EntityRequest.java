package com.richard.weger.wqc.rest.entity;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.Request;
import com.richard.weger.wqc.rest.RequestParameter;

import org.springframework.http.HttpEntity;

import java.net.URI;
import java.util.List;

public class EntityRequest<T extends DomainEntity> extends Request {

    private Class<T> clazz;
    private HttpEntity<T> entity;
    private EntityReturnType entityReturnType;

    public HttpEntity<T> getEntity() {
        return entity;
    }

    public void setEntity(HttpEntity<T> entity) {
        this.entity = entity;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public EntityReturnType getEntityReturnType() {
        return entityReturnType;
    }

    public void setEntityReturnType(EntityReturnType entityReturnType) {
        this.entityReturnType = entityReturnType;
    }

}
