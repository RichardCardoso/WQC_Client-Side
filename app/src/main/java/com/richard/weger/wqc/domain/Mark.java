package com.richard.weger.wqc.domain;

import java.util.List;

public class Mark extends ParentAwareEntity {
	
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}
	
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {

	}
	
	public Mark() {
		this.x = 0;
		this.y = 0;
		this.device = new Device();
		setParent(new Page());
	}

//	private int type;

	private float x;

	private float y;

	private Device device;
	
	private String roleToShow;
	
	private String addedOn;

//	public int getDescription() {
//		return type;
//	}
//
//	public void setDescription(int type) {
//		this.type = type;
//	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device user) {
		this.device = user;
	}

	public String getAddedOn() {
		return addedOn;
	}

	public void setAddedOn(String date) {
		this.addedOn = date;
	}

	public String getRoleToShow() {
		return roleToShow;
	}

	public void setRoleToShow(String ruleToShow) {
		this.roleToShow = ruleToShow;
	}

}
