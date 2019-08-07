package com.richard.weger.wqc.rest.file;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.entity.EntityReturnType;

import java.util.ArrayList;
import java.util.List;

public class RawFileRequest {
    private String requestCode;
    private String requestMethod;
    private String resource;
    private List<RequestParameter> parameters;
    private FileReturnType fileReturnType;

    public RawFileRequest(){
        setParameters(new ArrayList<>());
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

    public List<RequestParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<RequestParameter> parameters) {
        this.parameters = parameters;
    }

    public FileReturnType getFileReturnType() {
        return fileReturnType;
    }

    public void setFileReturnType(FileReturnType entityReturnType) {
        this.fileReturnType = entityReturnType;
    }

}
