package com.richard.weger.wqc.result;

public class SingleObjectResult<T> extends ResultWithContent<T> {
	
	public SingleObjectResult(Class<T> contentClz, T content) {
		super(contentClz);
		this.object = content;
	}

	T object;
	
	private boolean updated;

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public T getObject() {
		return object;
	}

}
