package com.richard.weger.wqc.domain;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class DomainEntity implements Serializable {

	public DomainEntity(Long id){
		this();
		setId(id);
	}
	
	public DomainEntity() {
		setType(getClass().getSimpleName());
	}

	private Long id = 0L;

	private Long version = 0L;
	
	private String type;

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
