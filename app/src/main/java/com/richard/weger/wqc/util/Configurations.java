package com.richard.weger.wqc.util;

import com.richard.weger.wqc.domain.Role;

import java.util.ArrayList;
import java.util.List;

public class Configurations {

    private String serverPath = "";
    private Long userId = 0L;
    private String username = "";
    private List<String> roles = new ArrayList<>();
    private String deviceId = "";


    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

}
