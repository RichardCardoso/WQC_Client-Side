package com.richard.weger.wegerqualitycontrol.domain;

import com.richard.weger.wegerqualitycontrol.util.StringHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project  implements Serializable {

	public Project(){
		number = "";
		drawingList = new ArrayList<Drawing>(){
			{add(new Drawing());}
		};
		setReportList(new ArrayList<Report>(){
			{
				add(new AutomationComponentsReport());
				add(new ControlCardReport());
				add(new ElectricReport());
				add(new FactoryTestReport());
			}
		});
	}

	private String number;

	private List<Drawing> drawingList;

	private List<Report> reportList;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<Drawing> getDrawingList() {
		return drawingList;
	}

	public void setDrawingList(List<Drawing> drawingList) {
		this.drawingList = drawingList;
	}

	public List<Report> getReportList() {
		return reportList;
	}

	public void setReportList(List<Report> reportList) {
		this.reportList = reportList;
	}
}
