package com.richard.weger.wqc.helper;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;

import java.util.ArrayList;

public class DeviceHelper {
    public static Device getCurrentDevice(){
        Configurations conf = ConfigurationsManager.getLocalConfig();
        Device device = new Device();
        device.setId(conf.getUserId());
        device.setDeviceid(conf.getDeviceId());
        device.setName(conf.getUsername());

        conf.getRoles().stream()
            .forEach(r -> {
                Role role = new Role();
                role.setDescription(r);
                device.getRoles().add(role);
            });

        device.setMarks(new ArrayList<>());
        return device;
    }

    public static boolean isOnlyRole(String role){
        Device device = getCurrentDevice();
        return device.getRoles().stream()
            .map(r -> r.getDescription().toLowerCase())
            .filter(r -> !r.equals(role.toLowerCase()))
            .count() == 0;
    }
}
