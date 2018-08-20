package com.richard.weger.wegerqualitycontrol.domain;

import java.io.Serializable;

public class Part implements Serializable {

	public Part(){
		number = 0;
	}

	private int number;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
}
