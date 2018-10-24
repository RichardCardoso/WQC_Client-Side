package com.richard.weger.wqc.domain;

import java.io.Serializable;

public class Part implements Serializable {
    private int id;

    private int number;

    private transient Part part;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }
}
