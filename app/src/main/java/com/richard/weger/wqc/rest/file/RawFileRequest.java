package com.richard.weger.wqc.rest.file;

import com.richard.weger.wqc.rest.RawRequest;

public class RawFileRequest extends RawRequest {
    private String requestCode;
    private String requestMethod;
    private String resource;
    private FileReturnType fileReturnType;

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

    public FileReturnType getFileReturnType() {
        return fileReturnType;
    }

    public void setFileReturnType(FileReturnType entityReturnType) {
        this.fileReturnType = entityReturnType;
    }

}
