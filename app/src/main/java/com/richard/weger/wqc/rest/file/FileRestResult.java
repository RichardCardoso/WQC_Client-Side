package com.richard.weger.wqc.rest.file;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.RestResult;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class FileRestResult extends RestResult {

    private byte[] content;

    private List<String> existingContent;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public List<String> getExistingContent() {
        return existingContent;
    }

    public void setExistingContent(List<String> existingContent) {
        this.existingContent = existingContent;
    }
}
