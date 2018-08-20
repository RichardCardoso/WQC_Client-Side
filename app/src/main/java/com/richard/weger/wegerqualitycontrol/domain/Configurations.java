package com.richard.weger.wegerqualitycontrol.domain;

import java.io.Serializable;

public class Configurations implements Serializable, Cloneable {
    private String constructionDrawingPath = "Konstruktion/";
    private String technicDatasheetPath = "Technik/Kundendaten/";
    private String drawingCode = "5031";
    private String datasheetCode = "5033";
    private String drawingExtension = ".pdf";
    private String datasheetExtension = ".pdf";
    private String rootPath = "Auftrag/";
    private String serverPath = "192.168.1.5/dados/publico/"; // "richard-ntbk/";
    private String serverUsername = "weger\\vendas1"; // "test";
    private String serverPassword = "enge1221"; // "test";
    private String yearPrefix = "20";
    private String appPassword = "147258369";

    public String getConstructionDrawingPath() {
        return constructionDrawingPath;
    }

    public void setConstructionDrawingPath(String constructionDrawingPath) {
        this.constructionDrawingPath = constructionDrawingPath;
    }

    public String getTechnicDatasheetPath() {
        return technicDatasheetPath;
    }

    public void setTechnicDatasheetPath(String technicDatasheetPath) {
        this.technicDatasheetPath = technicDatasheetPath;
    }

    public String getDrawingCode() {
        return drawingCode;
    }

    public void setDrawingCode(String drawingCode) {
        this.drawingCode = drawingCode;
    }

    public String getDatasheetCode() {
        return datasheetCode;
    }

    public void setDatasheetCode(String datasheetCode) {
        this.datasheetCode = datasheetCode;
    }

    public String getDrawingExtension() {
        return drawingExtension;
    }

    public void setDrawingExtension(String drawingExtension) {
        this.drawingExtension = drawingExtension;
    }

    public String getDatasheetExtension() {
        return datasheetExtension;
    }

    public void setDatasheetExtension(String datasheetExtension) {
        this.datasheetExtension = datasheetExtension;
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
}
