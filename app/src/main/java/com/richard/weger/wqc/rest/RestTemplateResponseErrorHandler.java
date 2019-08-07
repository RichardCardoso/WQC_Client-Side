package com.richard.weger.wqc.rest;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.exception.DataRecoverException;
import com.richard.weger.wqc.exception.NotFoundException;
import com.richard.weger.wqc.exception.StaleDataException;
import com.richard.weger.wqc.rest.entity.EntityRequest;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.util.App;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.*;

public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    private RestResult result;

    public RestTemplateResponseErrorHandler(RestResult result){
        super();
        this.result = result;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return(response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws ResourceAccessException, IOException {
        if(response.getStatusCode() == HttpStatus.CONFLICT){
            throw new StaleDataException(response.getHeaders().getFirst("message"));
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND){
            throw new NotFoundException(response.getHeaders().getFirst("message"));
        } else {
            throw new DataRecoverException(response.getHeaders().getFirst("message"));
        }
    }
}
