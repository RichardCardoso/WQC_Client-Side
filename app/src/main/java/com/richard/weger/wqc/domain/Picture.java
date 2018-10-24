package com.richard.weger.wqc.domain;

import java.io.Serializable;

public class Picture  implements Serializable{
	
	private int id;

	public Picture() {
		this.id = 0;
		this.caption = "";
		this.filePath = "";
	}

	private String caption;

	private String filePath;

	private transient Item item;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
}
