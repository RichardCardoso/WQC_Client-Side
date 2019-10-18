package com.richard.weger.wqc.rest.file;

import com.richard.weger.wqc.rest.Request;

import org.springframework.http.HttpEntity;

import java.util.List;

public class FileRequest extends Request {

    private HttpEntity<String> entity;
    private FileReturnType entityReturnType;

    public HttpEntity<String> getEntity() {
        return entity;
    }

    public void setEntity(HttpEntity<String> entity) {
        this.entity = entity;
    }

    public FileReturnType getFileReturnType() {
        return entityReturnType;
    }

    public void setFileReturnType(FileReturnType entityReturnType) {
        this.entityReturnType = entityReturnType;
    }

}
