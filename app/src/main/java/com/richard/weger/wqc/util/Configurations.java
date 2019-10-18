package com.richard.weger.wqc.util;

import java.util.ArrayList;
import java.util.List;

public class Configurations {
    private String serverPath = "";
    private long userId = 0;
    private String username = "";
    private List<String> roles;
    private String deviceId = "";

    public Configurations(){
        setRoles(new ArrayList<>());
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}