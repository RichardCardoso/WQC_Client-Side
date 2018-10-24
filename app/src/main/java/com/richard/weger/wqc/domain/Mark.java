package com.richard.weger.wqc.domain;

import java.io.Serializable;

public class Mark  implements Serializable{

	private int id;
	
	public Mark() {
		this.id = 0;
		this.type = 0;
		this.x = 0;
		this.y = 0;
		this.device = new Device();
	}

	private int type;

	private float x;

	private float y;

	private Device device;

	private transient Page page;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

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

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}
}
