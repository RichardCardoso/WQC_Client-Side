package com.richard.weger.wqc.rest;

import java.util.HashSet;
import java.util.Set;

public abstract class RawRequest {

    public RawRequest(){
        parameters = new HashSet<>();
    }

    private Set<RequestParameter> parameters;

    public Set<RequestParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<RequestParameter> parameters) {
        this.parameters = parameters;
    }

}
