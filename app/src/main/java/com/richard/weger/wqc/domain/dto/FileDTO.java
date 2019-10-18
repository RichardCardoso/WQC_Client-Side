package com.richard.weger.wqc.domain.dto;

import com.richard.weger.wqc.domain.DomainEntity;

@SuppressWarnings("serial")
public class FileDTO extends DomainEntity {
	
	public FileDTO() {
		super();
	}
	
	public FileDTO(String fileName, Long lastModifiedDate, double fileSize) {
		super();
		this.fileName = fileName;
		this.lastModifiedDate = lastModifiedDate;
		this.fileSize = fileSize;
	}
	
	private String fileName;
	private Long lastModifiedDate;
	private double fileSize;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Long getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Long lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public double getFileSize() {
		return fileSize;
	}
	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}
	
}
