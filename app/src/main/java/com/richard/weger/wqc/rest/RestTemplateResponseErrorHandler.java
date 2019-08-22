package com.richard.weger.wqc.rest;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.exception.ServerException;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.util.App;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    private String requestCode;
    private String resource;

    public RestTemplateResponseErrorHandler(String requestCode, String resource){
        super();
        this.requestCode = requestCode;
        this.resource = resource;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return(response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws ServerException {
        ErrorResult err;
        String sCode;
        String sLevel;
        ErrorResult.ErrorCode code;
        String description;
        ErrorResult.ErrorLevel level;
        HttpStatus responseStatus;

        sCode = null;
        sLevel = null;
        responseStatus = null;
        description = App.getContext().getResources().getString(R.string.unknownErrorMessage);

        try{
            responseStatus = response.getStatusCode();
        } catch (Exception ignored) {}

        if(responseStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            sCode = response.getHeaders().getFirst("code");
            sLevel = response.getHeaders().getFirst("level");
            description = response.getHeaders().getFirst("description");
        } else if (responseStatus == HttpStatus.NOT_FOUND){
            sCode = ErrorResult.ErrorCode.ENTITY_NOT_FOUND.toString();
            sLevel = ErrorResult.ErrorLevel.LOG.toString();
            description = "Resource: " + resource + ", request code: " + requestCode;
        }

        if(sCode != null && sLevel != null) {
            code = ErrorResult.ErrorCode.valueOf(sCode);
            level = ErrorResult.ErrorLevel.valueOf(sLevel);
        } else {
            code = ErrorResult.ErrorCode.UNKNOWN_ERROR;
            level = ErrorResult.ErrorLevel.SEVERE;
        }

        err = new ErrorResult(code, description, level, getClass());
        err.setRequestCode(requestCode);

        throw new ServerException(err);
    }
}
