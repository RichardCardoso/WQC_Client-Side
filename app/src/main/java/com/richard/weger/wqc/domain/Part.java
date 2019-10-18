package com.richard.weger.wqc.domain;

import java.util.List;

public class Part extends ParentAwareEntity {
	
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}
	
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		
	}
		
    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
	public DrawingRef getParent(){
    	return super.getParent();
	}

}
