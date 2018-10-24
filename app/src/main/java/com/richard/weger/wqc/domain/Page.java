package com.richard.weger.wqc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page  implements Serializable{

	private int id;

	private int number;

	public Page() {
		this.id = 0;
		this.number = 0;
		this.marks = new ArrayList<>();
		this.report = new CheckReport();
	}

	private transient CheckReport report;

	private List<Mark> marks;

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

	public List<Mark> getMarks() {
		return marks;
	}

	public void setMarks(List<Mark> mark) {
		this.marks = mark;
	}

	public CheckReport getReport() {
		return report;
	}

	public void setReport(CheckReport report) {
		this.report = report;
	}

}
