package com.richard.weger.wqc.rest;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.exception.DataRecoverException;
import com.richard.weger.wqc.util.App;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.util.List;

import static com.richard.weger.wqc.util.AppConstants.*;

public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    private RestTemplateHelper.HttpHelperResponse delegate;
    private String requestCode;
    private List<String> params;
    private String requestMethod;


    public RestTemplateResponseErrorHandler(RestTemplateHelper.HttpHelperResponse delegate, UriBuilder uriBuilder){
        super();
        this.delegate = delegate;
        this.requestCode = uriBuilder.getRequestCode();
        this.params = uriBuilder.getParameters();
        this.requestMethod = uriBuilder.getRequestMethod();
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return(response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if(response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR){
            if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                if(requestCode.equals(REST_QRPROJECTLOAD_KEY) && requestMethod.equals(GET_METHOD)){
                    RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
                    UriBuilder uriBuilder = new UriBuilder();
                    uriBuilder.setRequestCode(REST_QRPROJECTCREATE_KEY);
                    uriBuilder.getParameters().add(params.get(0));
                    restTemplateHelper.execute(uriBuilder);
                } else {
                    throw new DataRecoverException(App.getContext().getResources().getString(R.string.dataRecoverError));
                }
            } else if (response.getStatusCode() == HttpStatus.LOCKED) {
                throw new DataRecoverException(App.getContext().getResources().getString(R.string.drawingLockedMessage));
            } else{
                throw new HttpServerErrorException(response.getStatusCode(), App.getContext().getResources().getString(R.string.dataRecoverError));
            }
        }
    }
}
