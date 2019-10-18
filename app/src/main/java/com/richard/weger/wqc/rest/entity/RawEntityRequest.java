package com.richard.weger.wqc.rest.entity;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.RawRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RawEntityRequest<T extends DomainEntity> extends RawRequest {
    private String requestCode;
    private String requestMethod;
    private String resource;
    private List<T> entitiesList;
    private T singleEntity;
    private EntityReturnType entityReturnType;
    private String overriddenResource;

    public RawEntityRequest(T entity){
        this.setSingleEntity(entity);
        setParameters(new HashSet<>());
        setEntitiesList(new ArrayList<>());
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public List<T> getEntitiesList() {
        return entitiesList;
    }

    public void setEntitiesList(List<T> entitiesList) {
        this.entitiesList = entitiesList;
    }

    public T getSingleEntity() {
        return singleEntity;
    }

    public void setSingleEntity(T singleEntity) {
        this.singleEntity = singleEntity;
    }

    public EntityReturnType getEntityReturnType() {
        return entityReturnType;
    }

    public void setEntityReturnType(EntityReturnType entityReturnType) {
        this.entityReturnType = entityReturnType;
    }

    public String getOverriddenResource() {
        return overriddenResource;
    }

    public void setOverriddenResource(String overriddenResource) {
        this.overriddenResource = overriddenResource;
    }

}
