package com.richard.weger.wqc.domain;

import java.io.Serializable;

public class AutomaticItem  implements Serializable{

	private int id;

	public AutomaticItem() {
		this.id = 0;
		this.description = "";
		this.remoteTag = "";
	}

	private String description;

	private String remoteTag;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRemoteTag() {
		return remoteTag;
	}

	public void setRemoteTag(String remoteTag) {
		this.remoteTag = remoteTag;
	}

}
