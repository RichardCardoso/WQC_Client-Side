package com.richard.weger.wqc.util;

public class GeneralPictureDTO {

    public GeneralPictureDTO(String fileName, boolean processed){
        this.fileName = fileName;
        this.processed = processed;
    }

    private String fileName;
    private boolean processed;
    private boolean error;
    private boolean processing;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
        if(processed){
            setProcessing(false);
        }
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
}
