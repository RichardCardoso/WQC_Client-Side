package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ItemReport extends Report {
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getItems();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setItems((List<Item>) children);
	}

	private List<Item> items;
	
	private String client;
	
	private String comments;

	public ItemReport() {
		this.items = new ArrayList<>();
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> item) {
		this.items = item;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
