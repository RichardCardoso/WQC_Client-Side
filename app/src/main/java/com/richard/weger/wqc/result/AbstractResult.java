package com.richard.weger.wqc.result;

import com.richard.weger.wqc.rest.Request;

public abstract class AbstractResult {
	private String requestCode;
	private Request request;

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public Request getRequest() {
        return request;
    }

    public <T extends Request> void setRequest(T request) {
        this.request = request;
    }
}
