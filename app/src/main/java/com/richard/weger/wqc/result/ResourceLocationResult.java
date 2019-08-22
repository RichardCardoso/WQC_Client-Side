package com.richard.weger.wqc.result;

public class ResourceLocationResult extends SuccessResult {

    private String location;

    public ResourceLocationResult(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

}
