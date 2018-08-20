package com.richard.weger.wegerqualitycontrol.domain;

import android.os.SystemClock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.ITEM_NOT_CHECKED_KEY;

public abstract class Report  implements Serializable {

	public Report(){
		date = new Date(SystemClock.currentThreadTimeMillis());
		progress = 0;
		itemList = new ArrayList<>();
		drawing = new Drawing();
		part = new Part();
		comments = "";
		client = "";
		commission = "";
		responsible = "";
		// pictureList = new ArrayList<>();
		fillItemsList();
	}

	@Override
	public abstract String toString();

	private String client;

	private String commission;

	private String responsible;

	private Date date;

	private int progress;
	// 0 = not started
	// 1 = in progress
	// 2 = finished
	// 3 = uploaded to the server

	protected List<Item> itemList;

	private Drawing drawing;

	private Part part;

	private String comments;

	// private List<Picture> pictureList;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public List<Item> getItemList() {
		return itemList;
	}

	public void setItemList(List<Item> item) {
		this.itemList = item;
	}

	public Drawing getDrawing() {
		return drawing;
	}

	public void setDrawing(Drawing drawing) {
		this.drawing = drawing;
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	protected abstract void fillItemsList();

	public int getPendingItemsCount(){
		int itemCount = 0;
		for(Item i:itemList){
			if(i.getStatus() == ITEM_NOT_CHECKED_KEY){
				itemCount ++;
			}
		}
		return itemCount;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getCommission() {
		return commission;
	}

	public void setCommission(String commission) {
		this.commission = commission;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}
}
