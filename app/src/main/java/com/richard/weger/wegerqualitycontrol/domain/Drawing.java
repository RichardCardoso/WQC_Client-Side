package com.richard.weger.wegerqualitycontrol.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Drawing  extends WQCDocument implements Serializable {

	public Drawing(){
		number = 0;
		part = new ArrayList<Part>(){
			{add(new Part());}
		};
		datasheet = new Datasheet();
	}

	private int number;

	private List<Part> part;

	private Datasheet datasheet;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<Part> getPart() {
		return part;
	}

	public void setPart(List<Part> part) {
		this.part = part;
	}

	public Datasheet getDatasheet() {
		return datasheet;
	}

	public void setDatasheet(Datasheet datasheet) {
		this.datasheet = datasheet;
	}
}
