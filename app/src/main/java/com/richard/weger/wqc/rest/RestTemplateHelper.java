package com.richard.weger.wqc.rest;

import android.os.AsyncTask;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.ProjectHandler;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;

import static com.richard.weger.wqc.util.AppConstants.*;


public class RestTemplateHelper extends AsyncTask<UriBuilder, Void, String> {

    public interface HttpHelperResponse{
        void RestTemplateCallback(String requestCode, String result);
    }

    private HttpHelperResponse delegate;
    private String requestCode;

    public RestTemplateHelper(HttpHelperResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(UriBuilder... uriBuilder) {
        RestTemplate restTemplate = new RestTemplate();
        String response = "";

        this.requestCode = uriBuilder[0].getRequestCode();
        (new UriHelper()).execute(uriBuilder[0]);
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler(delegate, uriBuilder[0]));

        try {
            if(uriBuilder[0].getRequestMethod().equals(GET_METHOD)) {
                if (requestCode.equals(REST_PDFREPORTREQUEST_KEY)) {
                    restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
                    HttpHeaders headers = new HttpHeaders();
                    headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
                    HttpEntity<String> entity = new HttpEntity<String>(headers);

                    ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.GET, entity, byte[].class);

//                    responseEntity = restTemplate.getForEntity(uriBuilder[0].getUri(), ResponseEntity.class);
                    byte[] contents = responseEntity.getBody();
                    (new ProjectHandler()).byteArrayToFile(contents, uriBuilder[0].getProject(), uriBuilder[0]);
                    response = uriBuilder[0].getParameters().get(0);
                } else if (requestCode.equals(REST_FIRSTCONNECTIONTEST_KEY)){
                    response = restTemplate.getForObject(uriBuilder[0].getUri(), response.getClass());
                } else if (requestCode.equals(REST_IDENTIFY_KEY)){
                    response = restTemplate.getForObject(uriBuilder[0].getUri(), response.getClass());
                } else {
                    response = restTemplate.getForObject(uriBuilder[0].getUri(), response.getClass());
                    if (response == null) {
                        response = "";
                    }
                }
            } else if (uriBuilder[0].getRequestMethod().equals(POST_METHOD)){
                if(requestCode.equals(REST_MARKSAVE_KEY)) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Mark> entity = new HttpEntity<>(uriBuilder[0].getMark(), headers);
                    URI uri = restTemplate.postForLocation(uriBuilder[0].getUri(), entity);
                    response = uri.toString();
                    if (!response.equals("")) {
                        uriBuilder[0].setRequestCode(REST_MARKLOAD_KEY);
                        (new UriHelper()).execute(uriBuilder[0], response);
                        return doInBackground(uriBuilder);
                    }
                } else {
                    URI uri = restTemplate.postForLocation(uriBuilder[0].getUri(), null);
                    response = uri.toString();
                    if (!response.equals("")) {
                        uriBuilder[0].setRequestCode(REST_QRPROJECTLOAD_KEY);
                        (new UriHelper()).execute(uriBuilder[0]);
                        return doInBackground(uriBuilder);
                    }
                }
            } else if (uriBuilder[0].getRequestMethod().equals(PUT_METHOD)){
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                ResponseEntity<String> responseEntity = null;
                if(requestCode.equals(REST_PROJECTSAVE_KEY)) {
                    HttpEntity<Project> entity = new HttpEntity<>(uriBuilder[0].getProject(), headers);
                    responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.PUT, entity, String.class);
                } else if (requestCode.equals(REST_ASKAUTHORIZATION_KEY)){
                    HttpEntity<Device> entity = new HttpEntity<>(uriBuilder[0].getDevice(), headers);
                    responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.PUT, entity, String.class);
                } else if (requestCode.equals(REST_REPORTITEMSSAVE_KEY)){
                    HttpEntity<Report> entity = new HttpEntity<>(uriBuilder[0].getReport(), headers);
                    responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.PUT, entity, String.class);
                }
                if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
                    return "ok";
                } else {
                    return null;
                }
            } else if (uriBuilder[0].getRequestMethod().equals(DELETE_METHOD)){
                if(requestCode.equals(REST_MARKREMOVE_KEY)){
                    restTemplate.delete(uriBuilder[0].getUri());
                    response = "Ok";
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
            if(ex.getMessage().equals(App.getContext().getResources().getString(R.string.dataRecoverError))){
                return ex.getMessage();
            } else {
                return null;
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.RestTemplateCallback(requestCode, result);
    }

}