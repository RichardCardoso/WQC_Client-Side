package com.richard.weger.wqc.domain;

import java.util.Map;

import com.richard.weger.wqc.constants.AppConstants;

public abstract class FactoryProject {
	public static Project getProject(Map<String, String> mapValues) {
		Project p = new Project();
		AppConstants c = new AppConstants();
		if (mapValues != null 
				&& mapValues.containsKey(c.DRAWING_NUMBER_KEY)
				&& mapValues.containsKey(c.PROJECT_NUMBER_KEY)) {
			
			p.setReference(mapValues.get(c.PROJECT_NUMBER_KEY));
			DrawingRef d = new DrawingRef();
			d.setProject(p);
			d.setNumber(Integer.valueOf(mapValues.get(c.DRAWING_NUMBER_KEY)));
			p.getDrawingRefs().add(d);
			
		}
		return p;
	}
}
