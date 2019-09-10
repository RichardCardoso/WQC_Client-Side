package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

public class CheckReport extends Report {
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getPages();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setPages((List<Page>) children);
	}

	public CheckReport() {
		this.pages = new ArrayList<Page>();
		this.fileName = "";
	}

	private List<Page> pages;
	private String fileName;

	public List<Page> getPages() {
		return pages;
	}
	
	public Page getPage(long id) {
		for(Page p : pages) {
			if(p.getId() == id) {
				return p;
			}
		}
		return null;
	}

	public void setPages(List<Page> page) {
		this.pages = page;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String serverPdfPath) {
		this.fileName = serverPdfPath;
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
		for(Page p : getPages()){
			cnt += p.getMarks().size();
		}
		return cnt;
	}

}
