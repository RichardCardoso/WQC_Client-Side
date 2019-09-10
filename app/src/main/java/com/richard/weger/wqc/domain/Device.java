package com.richard.weger.wqc.domain;

import com.richard.weger.wqc.util.App;

import java.util.ArrayList;
import java.util.List;

public class Device extends AuditableEntity {
	
	public Device() {
		this.marks = new ArrayList<>();
		this.roles = new ArrayList<>();
		this.enabled = true;
		this.setDeviceid(App.getUniqueId());
	}
		
	private String deviceid;
	private String name;
	private boolean enabled;

	private List<Role> roles;

	private List<Mark> marks;
	
	public String getDeviceid() {
		return deviceid;
	}
	public void setDeviceid(String deviceId) {
		this.deviceid = deviceId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Mark> getMarks() {
		return marks;
	}
	public void setMarks(List<Mark> marks) {
		this.marks = marks;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
}
