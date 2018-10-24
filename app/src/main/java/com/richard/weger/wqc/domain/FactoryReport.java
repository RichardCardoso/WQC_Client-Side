package com.richard.weger.wqc.domain;

public abstract class FactoryReport {
	public static Report getReport(String type) {
		try {
			Class c = Class.forName(type);
			Report r = (Report) c.newInstance();
			String className = type.substring(type.lastIndexOf('.') + 1, type.length());
			r.setType(className);
			return r;
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		}
		return null;
	}
}
