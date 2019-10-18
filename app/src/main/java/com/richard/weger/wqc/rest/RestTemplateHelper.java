package com.richard.weger.wqc.rest;

import android.os.AsyncTask;

import com.richard.weger.wqc.converter.MyHttpMessageConverter;
import com.richard.weger.wqc.exception.ServerException;
import com.richard.weger.wqc.helper.ActivityHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.service.ErrorResponseHandler;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.ErrorUtil;
import com.richard.weger.wqc.util.LoggerManager;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.logging.Logger;

public abstract class RestTemplateHelper<Params extends Request> extends AsyncTask<Params, Void, AbstractResult> {

    public interface RestResponseHandler {
        void RestTemplateCallback(AbstractResult result);
        void toggleControls(boolean resume);
        void runOnUiThread(Runnable runnable);
        void onFatalError();
    }

    public RestTemplateHelper(RestResponseHandler delegate, boolean toggleControlsOnCompletion){
        this.delegate = delegate;
        this.toggleControlsOnCompletion = toggleControlsOnCompletion;
        logger = LoggerManager.getLogger(RestTemplateHelper.class);
    }

    protected abstract AbstractResult executionStrategy(RestTemplate restTemplate, Params request) throws Exception;

    @SafeVarargs
    @Override
    protected final AbstractResult doInBackground(Params... req) {
        RestTemplate restTemplate = new RestTemplate();
        AbstractResult result = null;

        try{
            Params request;

            request = req[0];
            requestCode = request.getRequestCode();
            restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler(requestCode, request.getUri().toString()));
            restTemplate.getMessageConverters().add(0, new MyHttpMessageConverter());

            try {
                delegate.runOnUiThread(() -> ActivityHelper.disableHandlerControls(delegate, true));
            } catch (Exception ex) {
                fatalUnknownException(ex);
            }

             result = executionStrategy(restTemplate, request);

        } catch (ServerException ex){
            return ex.getErr();
        } catch (Exception ex){
            fatalUnknownException(ex);
        }

        if(result == null) {
            result = new ErrorResult(ErrorResult.ErrorCode.REST_OPERATION_ERROR, ErrorUtil.getUnknownErrorMessage(), ErrorResult.ErrorLevel.SEVERE);
        }
        result.setRequestCode(requestCode);
        result.setRequest(req[0]);

        return result;
    }

    protected RestResponseHandler delegate;
    protected String requestCode;
    private Logger logger;
    private boolean toggleControlsOnCompletion;

    @Override
    protected final void onPostExecute(AbstractResult result) {
        try {
            if(toggleControlsOnCompletion) {
                delegate.runOnUiThread(() -> delegate.toggleControls(true));
            }
        } catch (Exception ignored){

        }
        try {
            delegate.RestTemplateCallback(result);
        } catch (Exception ex){
            String message = StringHelper.getStackTraceAsString(ex);
            ErrorResult err = new ErrorResult(ErrorResult.ErrorCode.REST_POST_EXECUTOR_ERROR, message, ErrorResult.ErrorLevel.SEVERE);
            ErrorResponseHandler.handle(err, App.getContext(), null);
            try{
                delegate.onFatalError();
            } catch (Exception e2) {
                fatalUnknownException(e2);
            }
        }
    }

    private void fatalUnknownException(Exception ex){
        logger.severe(StringHelper.getStackTraceAsString(ex));
    }

    protected final <E, S> ResponseEntity<S> getResponseEntity(Class<S> responseClazz, HttpEntity<E> entity, URI uri, HttpMethod method, RestTemplate restTemplate) {
        String url = "unknown";
        try{
            url = uri.toURL().toString();
        } catch (Exception ignored){}
        logger.info("Started 'get' (exchange) request (code: " + requestCode + ", url:" + url + ") ");
        ResponseEntity<S> responseEntity = restTemplate.exchange(uri, method, entity, responseClazz);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK) && !responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            logger.warning("Request resulted in error code " + responseEntity.getStatusCode());
        }
        return responseEntity;
    }

    protected final <E> URI getLocation(HttpEntity<E> entity, URI uri, RestTemplate restTemplate){
        URI ret;
        ret = restTemplate.postForLocation(uri, entity);
        return ret;
    }

    protected final <E, S> ResponseEntity<S> getResponseEntity(ParameterizedTypeReference<S> responseType, HttpEntity<E> entity, URI uri, HttpMethod method, RestTemplate restTemplate) {
        String url = "unknown";
        try{
            url = uri.toURL().toString();
        } catch (Exception ignored){}
        logger.info("Started 'get' (exchange) request (code: " + requestCode + ", url:" + url + ") ");
        ResponseEntity<S> responseEntity = restTemplate.exchange(uri, method, entity, responseType);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK) && !responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            logger.warning("Request resulted in error code " + responseEntity.getStatusCode());
        }
        return responseEntity;
    }
}
