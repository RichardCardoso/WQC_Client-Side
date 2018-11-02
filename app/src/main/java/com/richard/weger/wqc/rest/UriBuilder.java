package com.richard.weger.wqc.rest;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.util.ItemsMissingPictures;

import org.springframework.http.HttpEntity;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class UriBuilder {
    private URI uri;
    private String requestMethod;
    private List<String> parameters;
    private String requestCode;
    private Project project;
    private Mark mark;
    private Page page;
    private HttpEntity<Serializable> httpEntity;
    private Device device;
    private Report report;
    private Item item;
    private List<Item> missingPictures;

    public UriBuilder(){
        setParameters(new ArrayList<String>());
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Mark getMark() {
        return mark;
    }

    public void setMark(Mark mark) {
        this.mark = mark;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public HttpEntity<Serializable> getHttpEntity() {
        return httpEntity;
    }

    public void setHttpEntity(HttpEntity<Serializable> httpEntity) {
        this.httpEntity = httpEntity;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Item>  getMissingPictures() {
        return missingPictures;
    }

    public void setMissingPictures(List<Item>  missingPictures) {
        this.missingPictures = missingPictures;
    }
}
