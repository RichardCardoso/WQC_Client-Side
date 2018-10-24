package com.richard.weger.wqc.domain;

import java.io.Serializable;

public class Item  implements Serializable{
	
	private int id;

	private int number;

	private String description;

	private String comments;

	private int status;

	private transient ItemReport itemReport;

	public Item() {
		this.id = 0;
		this.number = 0;
		this.description = "";
		this.comments = "";
		this.status = 0;
		this.picture = new Picture();
		this.itemReport = new ItemReport();
	}

	private Picture picture;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public ItemReport getItemReport() {
		return itemReport;
	}

	public void setItemReport(ItemReport itemReport) {
		this.itemReport = itemReport;
	}
}
