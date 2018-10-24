package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;
import static com.richard.weger.wqc.util.AppConstants.*;

public class ItemReport extends Report {

	private List<Item> items;

	protected ItemReport() {
		this.items = new ArrayList<>();
		super.setType(this.getClass().getName());
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

}
