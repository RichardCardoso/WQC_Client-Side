package com.richard.weger.wqc.domain;

import com.richard.weger.wqc.helper.ReportHelper;

import java.util.Date;

public abstract class Report extends ParentAwareEntity {
	
	protected Report() {
		this.reference = "";
		setParent(new DrawingRef());
	}

	private boolean finished;
	
	private String reference;

	public DrawingRef getParent(){
		return super.getParent();
	}

	@Override
	public String toString(){
		return reference + " - " + (new ReportHelper()).getReportLabel(getReference());
	}

//	public String getDescription() {
//		return type;
//	}
//
//	/**
//	 * Set the report's type
//	 * 
//	 * @param type
//	 *            Report's type
//	 *            0 = none, 1 = ItemsReport, 2 = CheckReport, 3 = AutomaticReport
//	 */
//	public void setDescription(String type) {
//		this.type = type;
//	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
