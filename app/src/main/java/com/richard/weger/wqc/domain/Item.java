package com.richard.weger.wqc.domain;

import java.util.List;

public class Item extends ParentAwareEntity {
		
	private int number;

	private String description;

	private String comments;

	private int status;

	private Picture picture;
	
	public Item(int number, String description) {
		this();
		this.number = number;
		this.description = description;
	}

	public Item() {
		this.number = 0;
		this.description = "";
		this.comments = "";
		this.status = 0;
		this.picture = new Picture();
		setParent(new ItemReport());
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Picture getPicture() {
		return picture;
	}

	public void setPicture(Picture picture) {
		this.picture = picture;
	}

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}
	
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {

	}

	public ItemReport getParent(){
		return super.getParent();
	}

}
