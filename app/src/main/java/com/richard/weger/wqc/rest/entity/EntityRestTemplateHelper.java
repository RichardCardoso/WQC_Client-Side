package com.richard.weger.wqc.rest.entity;

import android.os.AsyncTask;

import com.richard.weger.wqc.converter.MyHttpMessageConverter;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.exception.NotFoundException;
import com.richard.weger.wqc.exception.StaleDataException;
import com.richard.weger.wqc.rest.RestTemplateResponseErrorHandler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.DELETE_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.GET_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.POST_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_MARKREMOVE_KEY;
import static com.richard.weger.wqc.helper.LogHelper.writeData;


public class EntityRestTemplateHelper<T extends DomainEntity> extends AsyncTask<EntityRequest<T>, Void, EntityRestResult<T>> {

    public interface EntityRestResponse {
        <T extends DomainEntity> void EntityRestCallback(EntityRestResult<T> result);
        void toggleControls(boolean resume);
        void runOnUiThread(Runnable runnable);
        void onError();
    }

    private EntityRestResponse delegate;
    private String requestCode;
    private boolean toggleControlsOnCompletion = false;

    public EntityRestTemplateHelper(EntityRestResponse delegate, boolean toggleControlsOnCompletion){
        this.delegate = delegate;
        this.toggleControlsOnCompletion = toggleControlsOnCompletion;
    }

    @SafeVarargs
    @Override
    protected final EntityRestResult<T> doInBackground(EntityRequest<T>... req) {
        RestTemplate restTemplate = new RestTemplate();
        EntityRequest<T> request = req[0];
        EntityRestResult<T> result = new EntityRestResult<>();

        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler(result));
        restTemplate.setMessageConverters(Collections.singletonList(new MyHttpMessageConverter()));

        try {
            delegate.runOnUiThread(() -> delegate.toggleControls(false));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        requestCode = request.getRequestCode();
        result.setRequestCode(requestCode);

        try {
            if(request.getRequestMethod().equals(GET_METHOD)) {
                if (request.getEntityReturnType() == EntityReturnType.EntityListReturn) {
                    ParameterizedTypeReference<List<T>> type = new ParameterizedTypeReference<List<T>>() {};
                    ResponseEntity<List<T>> response = getResponseEntity(type, request.getEntity(), request.getUri(), restTemplate);

                    result.setEntities(response.getBody());
                    result.setStatus(response.getStatusCode());
                    result.setMessage(response.getHeaders().getFirst("message"));
                } else if (request.getEntityReturnType() == EntityReturnType.SingleEntityReturn){
                    ResponseEntity<T> response = getResponseEntity(request.getClazz(), request.getEntity(), request.getUri(), HttpMethod.GET, restTemplate);

                    result.getEntities().add(response.getBody());
                    result.setStatus(response.getStatusCode());
                    result.setMessage(response.getHeaders().getFirst("message"));
                }
            } else if (request.getRequestMethod().equals(POST_METHOD)){

                ResponseEntity<T> response = getResponseEntity(request.getClazz(), request.getEntity(), request.getUri(), HttpMethod.POST, restTemplate);

                if(response.getBody() != null) {
                    result.setEntities(Collections.singletonList(response.getBody()));
                    result.setMessage(response.getHeaders().getFirst("message"));
                } else {
                    result.setMessage(response.getHeaders().getFirst("location"));
                }
                result.setStatus(response.getStatusCode());
            } else if (request.getRequestMethod().equals(DELETE_METHOD)){
                if(requestCode.equals(REST_MARKREMOVE_KEY)){
                    ResponseEntity<T> responseEntity = getResponseEntity(request.getClazz(), null, request.getUri(), HttpMethod.DELETE, restTemplate);
                    // delete(request.getUri(), restTemplate);

                    result.setMessage(responseEntity.getHeaders().getFirst("message"));
                    result.setStatus(responseEntity.getStatusCode());
                    result.setEntities(null);
                }
            }
        } catch (Exception ex){

            try{
                delegate.onError();
            } catch (Exception ignored){}

            printStackTrace(ex);
            result.setEntities(null);
            if(ex instanceof NotFoundException){
                result.setStatus(HttpStatus.NOT_FOUND);
            } else if (ex instanceof StaleDataException){
                result.setStatus(HttpStatus.CONFLICT);
            } else {
                result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return result;
    }

    private void printStackTrace(Exception ex){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sStackTrace = sw.toString();
        writeData(sStackTrace);
    }

    private <E, R> ResponseEntity<R> getResponseEntity(Class<R> responseClazz, HttpEntity<E> entity, URI uri, HttpMethod method, RestTemplate restTemplate){
        String url = "unknown";
        try{
            url = uri.toURL().toString();
        } catch (Exception ignored){}
        writeData("Started '" + method.name() + "' (exchange) request (code: " + requestCode + ", url:" + url + ") ");
        ResponseEntity<R> responseEntity = restTemplate.exchange(uri, method, entity, responseClazz);
        writeData("Got 'get' request response (code: " + requestCode + ")");
        if( !responseEntity.getStatusCode().equals(HttpStatus.OK) && !responseEntity.getStatusCode().equals(HttpStatus.CREATED)){
            writeData("Request resulted in error code " + responseEntity.getStatusCode());
        }
        return responseEntity;
    }

    private <E, R> ResponseEntity<R> getResponseEntity(ParameterizedTypeReference<R> responseType, HttpEntity<E> entity, URI uri, RestTemplate restTemplate){
        String url = "unknown";
        try{
            url = uri.toURL().toString();
        } catch (Exception ignored){}
        writeData("Started 'get' (exchange) request (code: " + requestCode + ", url:" + url + ") ");
        ResponseEntity<R> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, responseType);
        writeData("Got 'get' request response (code: " + requestCode + ")");
        if(!responseEntity.getStatusCode().equals(HttpStatus.OK) && !responseEntity.getStatusCode().equals(HttpStatus.CREATED)){
            writeData("Request resulted in error code " + responseEntity.getStatusCode());
        }
        return responseEntity;
    }

    @Override
    protected void onPostExecute(EntityRestResult<T> result) {
        try {
            if(toggleControlsOnCompletion) {
                delegate.runOnUiThread(() -> delegate.toggleControls(true));
            }
        } catch (Exception ignored){}
        delegate.EntityRestCallback(result);
    }

}