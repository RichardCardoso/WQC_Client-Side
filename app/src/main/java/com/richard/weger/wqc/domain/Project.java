package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

public class Project extends ParentAwareEntity {

	private String reference;

	private List<DrawingRef> drawingRefs;

	public Project() {
		this.reference = "";
		this.drawingRefs = new ArrayList<>();
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

	public void setDrawingRefs(List<DrawingRef> drawingRef) {
		this.drawingRefs = drawingRef;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getDrawingRefs();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setDrawingRefs((List<DrawingRef>) children);
	}

}
