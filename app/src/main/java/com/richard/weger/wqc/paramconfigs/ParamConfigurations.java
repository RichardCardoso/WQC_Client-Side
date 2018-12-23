package com.richard.weger.wqc.paramconfigs;

import java.io.Serializable;

public class ParamConfigurations implements Serializable {
	
	protected ParamConfigurations() {
		
	}
	
	private int id = 1;
	private String originalDocsPath = "";
	private String wiredDrawingCode = "";
	private String wiredDatasheetCode = "";
	private String controlCardReportCode = "0000";
	private String cablelessDrawingCode = "";
	private String cablelessDatasheetCode = "";
	private String originalDocsExtension = "";
	private String rootPath = "";
	private String serverPath = "";
	private String serverUsername = "";
	private String serverPassword = "";
	private String yearPrefix = "";
	private String appPassword = "";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOriginalDocsPath() {
		return originalDocsPath;
	}

	public void setOriginalDocsPath(String technicDatasheetPath) {
		this.originalDocsPath = technicDatasheetPath;
	}

	public String getWiredDrawingCode() {
		return wiredDrawingCode;
	}

	public void setWiredDrawingCode(String drawingCode) {
		this.wiredDrawingCode = drawingCode;
	}

	public String getWiredDatasheetCode() {
		return wiredDatasheetCode;
	}

	public void setWiredDatasheetCode(String wiredDatasheetCode) {
		this.wiredDatasheetCode = wiredDatasheetCode;
	}

	public String getCablelessDrawingCode() {
		return cablelessDrawingCode;
	}

	public void setCablelessDrawingCode(String cablelessDrawingCode) {
		this.cablelessDrawingCode = cablelessDrawingCode;
	}

	public String getOriginalDocsExtension() {
		return originalDocsExtension;
	}

	public void setOriginalDocsExtension(String drawingExtension) {
		this.originalDocsExtension = drawingExtension;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getServerPath() {
		return serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

	public String getYearPrefix() {
		return yearPrefix;
	}

	public void setYearPrefix(String yearPrefix) {
		this.yearPrefix = yearPrefix;
	}

	public String getServerUsername() {
		return serverUsername;
	}

	public void setServerUsername(String serverUsername) {
		this.serverUsername = serverUsername;
	}

	public String getServerPassword() {
		return serverPassword;
	}

	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	public String getAppPassword() {
		return appPassword;
	}

	public void setAppPassword(String appPassword) {
		this.appPassword = appPassword;
	}

	public String getControlCardReportCode() {
		return controlCardReportCode;
	}

	public void setControlCardReportCode(String controlCardReportCode) {
		this.controlCardReportCode = controlCardReportCode;
	}

	public String getCablelessDatasheetCode() {
		return cablelessDatasheetCode;
	}

	public void setCablelessDatasheetCode(String cablelessDatasheetCode) {
		this.cablelessDatasheetCode = cablelessDatasheetCode;
	}
}
