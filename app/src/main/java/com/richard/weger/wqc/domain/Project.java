package com.richard.weger.wqc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable{

	private int id;
	
	private String reference;

	private List<DrawingRef> drawingRefs;

	public Project() {
		this.reference = "";
		this.drawingRefs = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public List<DrawingRef> getDrawingRefs() {
		return drawingRefs;
	}

	public void setDrawingRefs(List<DrawingRef> drawingRefs) {
		this.drawingRefs = drawingRefs;
	}

}
