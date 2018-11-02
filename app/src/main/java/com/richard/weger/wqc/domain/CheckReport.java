package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

public class CheckReport extends Report {

	protected CheckReport() {
		this.pages = new ArrayList<>();
		this.fileName = "";
	}

	private List<Page> pages;
	private String fileName;

	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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
