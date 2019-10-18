package com.richard.weger.wqc.exception;

import com.richard.weger.wqc.result.ErrorResult;

import org.springframework.web.client.RestClientException;

public class ServerException extends RestClientException {

    private ErrorResult err;

    public ServerException(ErrorResult err) {
        super(err.getDescription());
        this.err = err;
    }

    public ErrorResult getErr() {
        return err;
    }
}
