package com.richard.weger.wqc.appconstants;

public abstract class FactoryAppConstants {
	
	public static AppConstants INSTANCE;
	
	public static AppConstants getAppConstants() {
		if(INSTANCE == null) {
			INSTANCE = new AppConstants();
		}
		return INSTANCE;
	}
	
}
