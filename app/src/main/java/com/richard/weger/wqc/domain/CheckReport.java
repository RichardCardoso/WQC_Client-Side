package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

public class CheckReport extends Report {

	protected CheckReport() {
		this.pages = new ArrayList<>();
		this.clientPdfPath = "";
		this.serverPdfPath = "";
	}

	private List<Page> pages;
	private String clientPdfPath;
	private String serverPdfPath;

	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public String getClientPdfPath() {
		return clientPdfPath;
	}

	public void setClientPdfPath(String clientPdfPath) {
		this.clientPdfPath = clientPdfPath;
	}

	public String getServerPdfPath() {
		return serverPdfPath;
	}

	public void setServerPdfPath(String serverPdfPath) {
		this.serverPdfPath = serverPdfPath;
	}
	
	public int getPagesCount() {
		return pages.size();
	}
	
	public void addBlankPage() {
		Page p = new Page();
		p.setNumber(getPagesCount() + 1);
		pages.add(p);
	}
	
	public void removePage(int id) {
		pages.remove(id);
	}

	public int getMarksCount(){
		int cnt = 0;
		for(Page p: getPages()){
			cnt += p.getMarks().size();
		}
		if(super.getReference().equals("5033")){
			int i = 0;
		}
		return cnt;
	}
}
