package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;
import static com.richard.weger.wqc.constants.AppConstants.*;

public class ItemReport extends Report {

	private List<Item> items;

	private String comments;

	private String client;

	protected ItemReport() {
		this.items = new ArrayList<>();
		super.setType(this.getClass().getName());
	}

	@Override
	public String toString(){
		return "Kontrollkarte";
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public int getPendingItemsCount(){
		int cnt = 0;
		for(Item i: items){
			if(i.getStatus() == ITEM_NOT_CHECKED_KEY) {
				cnt++;
			}
		}
		return cnt;
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
}
