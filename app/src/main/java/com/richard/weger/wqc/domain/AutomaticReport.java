package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

public class AutomaticReport extends Report {

	public AutomaticReport() {
		this.automaticItems = new ArrayList<AutomaticItem>();
	}

	private List<AutomaticItem> automaticItems;

	public List<AutomaticItem> getAutomaticItems() {
		return automaticItems;
	}

	public void setAutomaticItems(List<AutomaticItem> automaticItems) {
		this.automaticItems = automaticItems;
	}

}
