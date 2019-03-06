package com.richard.weger.wqc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DrawingRef implements Serializable {

	public DrawingRef() {
		this.id = 0;
		this.number = 0;
		this.reports = new ArrayList<Report>();
		this.project = new Project();
	}

	private int id;

	private int number;

	private List<Report> reports;
	
	private transient Project project;

	private List<Part> parts;

	private String currentUser;

	private Date lastEditTime;

	private boolean finished;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<Report> getReports() {
		return reports;
	}

	public Report getReport(int reportId){
		for(Report r : getReports()){
			if(r.getId() == reportId){
				return r;
			}
		}
		return null;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<Part> getParts() {
		return parts;
	}

	public void setParts(List<Part> parts) {
		this.parts = parts;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public Date getLastEditTime() {
		return lastEditTime;
	}

	public void setLastEditTime(Date lastEditTime) {
		this.lastEditTime = lastEditTime;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
