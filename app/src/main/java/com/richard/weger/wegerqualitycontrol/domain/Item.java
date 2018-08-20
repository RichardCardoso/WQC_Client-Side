package com.richard.weger.wegerqualitycontrol.domain;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Item implements Serializable{

	public Item(){
		initializeFields();
	}

	public Item(String description){
		initializeFields();
		setDescription(description);
	}

	public Item(int id, String description){
		initializeFields();
		setId(id);
		setDescription(description);
	}

	private void initializeFields(){
		id = 0;
		description = "";
		picture = new Picture();
		comments = "";
		status = 0;
	}

	private int id;

	private String description;

	private Picture picture;

	private String comments;

	private int status;
	// 0 - not started
	// 1 - approved
	// 2 - reproved
	// 3 - not applicable

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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id + " - " + description;
	}

	public Picture getPicture() {
		return picture;
	}

	public void setPicture(Picture picture) {
		this.picture = picture;
	}
}
