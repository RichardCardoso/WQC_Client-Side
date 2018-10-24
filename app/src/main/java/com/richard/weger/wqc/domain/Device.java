package com.richard.weger.wqc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Device implements Serializable {
	
	public Device() {
		this.marks = new ArrayList<>();
	}
	
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	private String deviceid;
	private String name;
	private String role;
	private boolean enabled;

	private transient List<Mark> marks;
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
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
	
}
