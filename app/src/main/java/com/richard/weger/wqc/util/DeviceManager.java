package com.richard.weger.wqc.util;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Mark;

import java.util.ArrayList;

public class DeviceManager {
    public static Device getCurrentDevice(){
        Configurations conf = ConfigurationsManager.getLocalConfig();
        Device device = new Device();
        device.setId(conf.getUserId());
        device.setDeviceid(conf.getDeviceId());
        device.setName(conf.getUsername());
        device.setRole(conf.getRole());
        device.setMarks(new ArrayList<Mark>());
        return device;
    }
}
