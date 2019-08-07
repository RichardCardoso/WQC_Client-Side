package com.richard.weger.wqc.rest;

import java.net.URI;
import java.util.List;

public class Request {

    private URI uri;
    private List<RequestParameter> parameters;
    private String requestCode;
    private String requestMethod;


    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public List<RequestParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<RequestParameter> parameters) {
        this.parameters = parameters;
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

}
