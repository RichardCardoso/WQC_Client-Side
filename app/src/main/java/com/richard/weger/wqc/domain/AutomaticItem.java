package com.richard.weger.wqc.domain;

import java.util.List;

public class AutomaticItem extends ParentAwareEntity {
		
	public AutomaticItem() {
		this.description = "";
		this.remoteTag = "";
		setParent(new AutomaticReport());
	}

	private String description;

	private String remoteTag;

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

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}
	
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		
	}

}
