package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

public class DrawingRef extends ParentAwareEntity {
	
	public DrawingRef() {
		this.dnumber = 0;
		this.reports = new ArrayList<>();
		this.parts = new ArrayList<>();
		setParent(new Project());
	}

	private int dnumber;

	private List<Report> reports;

	private List<Part> parts;

	public int getDnumber() {
		return dnumber;
	}

	public void setDnumber(int dnumber) {
		this.dnumber = dnumber;
	}

	public List<Report> getReports() {
		return reports;
	}

	private void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public List<Part> getParts() {
		return parts;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getReports();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setReports((List<Report>) children);
	}

	@SuppressWarnings("unchecked")
	public Project getParent(){
		return super.getParent();
	}

}
