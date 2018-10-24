package com.richard.weger.wqc.domain;

import java.io.Serializable;
import java.util.Date;

public class Report implements Serializable {

	protected Report() {
		this.id = 0;
		this.date = new Date();
		this.type = "";
		this.reference = "";
		this.drawingref = new DrawingRef();
	}

	private int id;

	private Date date;

	private String type;
	
	private String reference;
	
	private transient DrawingRef drawingref;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	/**
	 * Set the report's type
	 * 
	 * @param type
	 *            Report's type
	 *            0 = none, 1 = ItemsReport, 2 = CheckReport, 3 = AutomaticReport
	 */
	public void setType(String type) {
		this.type = type;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public DrawingRef getDrawingref() {
		return drawingref;
	}

	public void setDrawingref(DrawingRef drawingref) {
		this.drawingref = drawingref;
	}

	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

}
