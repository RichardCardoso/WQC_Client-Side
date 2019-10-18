package com.richard.weger.wqc.domain;

import java.util.List;

public class Picture extends ParentAwareEntity {
	
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}
	
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		
	}
	
	public Picture() {
		this.caption = "";
		this.fileName = "";
	}

	private String caption;

	private String fileName;

	private Item item;
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String filePath) {
		this.fileName = filePath;
	}

	public Item getItem() {
		return item;
	}

	public void setDrawingref(Item item) {
		this.item = item;
	}

}
