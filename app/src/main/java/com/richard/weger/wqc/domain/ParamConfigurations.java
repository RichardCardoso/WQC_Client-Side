package com.richard.weger.wqc.domain;

@SuppressWarnings("serial")
public class ParamConfigurations extends AuditableEntity {
	
	public ParamConfigurations() {
		setId(1L);
	}
	
	private String originalDocsPath = "Technik/Kundendaten/";
	private String controlCardReportCode = "0000";
	private String wiredDrawingCode = "5032";
	private String wiredDatasheetCode = "5033";
	private String cablelessDrawingCode = "5001";
	private String cablelessDatasheetCode = "5002";
	private String originalDocsExtension = ".pdf";
	private String rootPath = "Auftrag/";
	private String serverPath = "//srv-weger1/dados/publico/";
	private String serverUsername = "";
	private String serverPassword = "";
	private String yearPrefix = "20";
	private String appPassword = "147258369";

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

	public void setWiredDatasheetCode(String datasheetCode) {
		this.wiredDatasheetCode = datasheetCode;
	}

	public String getCablelessDrawingCode() {
		return cablelessDrawingCode;
	}

	public void setCablelessDrawingCode(String electricDrawingCode) {
		this.cablelessDrawingCode = electricDrawingCode;
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
