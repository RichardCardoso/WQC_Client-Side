package com.richard.weger.wqc.domain;

import java.util.List;

public abstract class ParentAwareEntity extends AuditableEntity {

	private transient DomainEntity parent;
	
	public abstract <T extends ParentAwareEntity> List<T> getChildren();
	public abstract <T extends ParentAwareEntity> void setChildren(List<T> children);

	@SuppressWarnings("unchecked")
	public <T extends DomainEntity> T getParent() {
		return (T) parent;
	}

	public <T extends DomainEntity>  void setParent(T parent) {
		this.parent = parent;
	}
}
