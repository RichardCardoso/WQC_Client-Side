package com.richard.weger.wqc.result;

public abstract class ResultWithContent<T> extends SuccessResult {
	
	private Class<T> contentClz;

	public Class<T> getContentClz() {
		return contentClz;
	}

	public ResultWithContent(Class<T> contentClz) {
		this.contentClz = contentClz;
	}

}
