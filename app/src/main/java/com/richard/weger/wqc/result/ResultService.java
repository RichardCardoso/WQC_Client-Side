package com.richard.weger.wqc.result;

import java.util.List;

import org.springframework.http.HttpHeaders;

@SuppressWarnings("unchecked")
public class ResultService {
	
	public static <T> SingleObjectResult<T> getSingleResultContainer(AbstractResult res, Class<T> clz) {
		SingleObjectResult<T> ret = null;
		if(res instanceof SingleObjectResult) {
			SingleObjectResult<?> rTest = (SingleObjectResult<?>) res;
			if(rTest.getContentClz().isAssignableFrom(clz)) {
				ret = (SingleObjectResult<T>) res;
			}
		}
		return ret;
	}

	public static String getLocationResult(AbstractResult res){
		String ret = null;
		if(res instanceof ResourceLocationResult){
			ret = ((ResourceLocationResult) res).getLocation();
		}
		return ret;
	}
	
	public static <T> T getSingleResult(AbstractResult res, Class<T> objectClass) {
		T ret = null;
		SingleObjectResult<T> rWork = getSingleResultContainer(res, objectClass);
		if(rWork != null) {
			ret = rWork.getObject();
		}
		return ret;
	}
	
	public static <T> List<T> getMultipleResult(AbstractResult res, Class<T> listContentClass) {
		List<T> lst = null;
		if(res instanceof MultipleObjectResult) {
			MultipleObjectResult<T> rWork = null;
			MultipleObjectResult<?> rTest = (MultipleObjectResult<?>) res;
			if(rTest.getContentClz().isAssignableFrom(listContentClass)) {
				rWork = (MultipleObjectResult<T>) res;
				lst = rWork.getObjects();
			}
		}
		return lst;
	}
	
	public static ErrorResult getErrorResult(AbstractResult res) {
		if(res instanceof ErrorResult) {
			return (ErrorResult) res;
		} else {
			return null;
		}
	}
	
	public static HttpHeaders getErrorHeaders(ErrorResult res) {
		ErrorResult err = ResultService.getErrorResult(res);
		HttpHeaders headers = new HttpHeaders();
		headers.set("code", String.valueOf(err.getCode()));
		headers.set("message", err.getDescription());
		headers.set("level", err.getLevel().toString());
		return headers;
	}
	
}
