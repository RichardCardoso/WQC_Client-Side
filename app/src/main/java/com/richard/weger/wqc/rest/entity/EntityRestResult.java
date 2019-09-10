package com.richard.weger.wqc.rest.entity;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.RestResult;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class EntityRestResult<T extends DomainEntity> extends RestResult {

    private List<T> entities;

    public EntityRestResult(){
        entities = new ArrayList<>();
    }

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

}
