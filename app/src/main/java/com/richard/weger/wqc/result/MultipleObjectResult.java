package com.richard.weger.wqc.result;

import java.util.List;

public class MultipleObjectResult<T> extends ResultWithContent<T> {
	

	public MultipleObjectResult(Class<T> contentClz, List<T> contents) {
		super(contentClz);
		this.objects = contents;
	}

	List<T> objects;

	public List<T> getObjects() {
		return objects;
	}
	
}
