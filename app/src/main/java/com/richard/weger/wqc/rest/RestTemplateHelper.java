package com.richard.weger.wqc.rest;

import android.os.AsyncTask;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.exception.DataRecoverException;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.helper.ProjectHelper;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static com.richard.weger.wqc.constants.AppConstants.*;


public class RestTemplateHelper extends AsyncTask<UriBuilder, Void, String> {

    public interface RestHelperResponse {
        void RestTemplateCallback(String requestCode, String result);
    }

    private RestHelperResponse delegate;
    private String requestCode;

    public RestTemplateHelper(RestHelperResponse delegate){
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
                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.GET, entity, byte[].class);

//                    responseEntity = restTemplate.getForEntity(uriBuilder[0].getUri(), ResponseEntity.class);
                    byte[] contents = responseEntity.getBody();
                    ProjectHelper.byteArrayToFile(contents, uriBuilder[0].getProject(), uriBuilder[0], "Originals/");
                    response = uriBuilder[0].getParameters().get(0);
                } else if (requestCode.equals(REST_PICTUREDOWNLOAD_KEY) || requestCode.equals(REST_GENPICTUREDOWNLOAD_KEY)){
                    restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
                    HttpHeaders headers = new HttpHeaders();
                    headers.setAccept(Arrays.asList(MediaType.IMAGE_JPEG));
                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.GET, entity, byte[].class);
                    byte[] contents = null;

                    if(responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                         contents = responseEntity.getBody();
                    }
                    ProjectHelper.byteArrayToFile(contents, uriBuilder[0].getProject(), uriBuilder[0], "Pictures/");
                    response = uriBuilder[0].getParameters().get(0);

                } else if (requestCode.equals(REST_FIRSTCONNECTIONTEST_KEY)){
                    response = restTemplate.getForObject(uriBuilder[0].getUri(), response.getClass());
                } else if (requestCode.equals(REST_IDENTIFY_KEY)) {
                    response = restTemplate.getForObject(uriBuilder[0].getUri(), response.getClass());
                } else if (requestCode.equals(REST_GENPICTURESREQUEST_KEY)) {
                    HttpHeaders headers = new HttpHeaders();

                    HttpEntity<List<String>> entity = new HttpEntity<>(headers);
                    ResponseEntity<String> responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.GET, entity, String.class);

                    if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
                        return responseEntity.getBody();
                    } else {
                        return null;
                    }
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
                } else if (requestCode.equals(REST_PICTUREUPLOAD_KEY) || requestCode.equals(REST_GENPICTUREUPLOAD_KEY)){
                    restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

                    LinkedMultiValueMap params = new LinkedMultiValueMap();
                    params.add("file", new FileSystemResource(new File(uriBuilder[0].getParameters().get(1))));
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    HttpEntity entity = new HttpEntity<>(params, headers);
                    ResponseEntity<String> responseEntity;
                    try {
                        responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.POST, entity, String.class);
                        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
                            if(requestCode.equals(REST_GENPICTUREUPLOAD_KEY)){
                                String originalFileName = responseEntity.getHeaders().getFirst("originalFileName");
                                String newFileName = responseEntity.getHeaders().getFirst("newFileName");
                                if(newFileName != null && !newFileName.equals(originalFileName)){
                                    String picFolder = StringHelper.getPicturesFolderPath(uriBuilder[0].getProject());
                                    FileHelper.fileCopy(new File(picFolder.concat(originalFileName)),
                                            new File(picFolder.concat(newFileName)));
                                    FileHelper.fileDelete(picFolder.concat(originalFileName));
                                    UriBuilder builder = uriBuilder[0];
                                    builder.setRequestCode(REST_GENPICTUREDOWNLOAD_KEY);
                                    builder.getParameters().clear();
                                    builder.getParameters().add(originalFileName);
                                    doInBackground(builder);
                                    return newFileName;
                                } else {
                                    return originalFileName;
                                }
                            }
                            return "ok";
                        } else {
                            return null;
                        }
                    } catch (Exception ex){
                        DataRecoverException e = (DataRecoverException) ex;
                        return App.getContext().getResources().getString(R.string.drawingLockedMessage).concat("###").concat(String.valueOf(e.id));
                    }
                } else if (requestCode.equals(REST_PICTURESREQUEST_KEY)) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<List<Item>> entity = new HttpEntity<>(uriBuilder[0].getMissingPictures(), headers);
                    ResponseEntity<String> responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.POST, entity, String.class);

                    if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
                        return responseEntity.getBody();
                    } else {
                        return null;
                    }
                } else if (requestCode.equals(REST_PROJECTUPLOAD_KEY)){
                    ResponseEntity<String> responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.POST, null, String.class);

                    if (responseEntity != null){
                        if(responseEntity.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR){
                            return responseEntity.getHeaders().getFirst("message");
                        } else {
                            return "ok";
                        }
                    } else {
                        return null;
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
                } else if (requestCode.equals(REST_ITEMSAVE_KEY)){
                    HttpEntity<Item> entity = new HttpEntity<>(uriBuilder[0].getItem(), headers);
                    responseEntity = restTemplate.exchange(uriBuilder[0].getUri(), HttpMethod.PUT, entity, String.class);
                }
                if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
                    return responseEntity.getBody();
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
            if(requestCode.equals(REST_PICTUREDOWNLOAD_KEY)){
                try {
                    ProjectHelper.byteArrayToFile(null, uriBuilder[0].getProject(), uriBuilder[0], "Pictures/");
                    return "";
                } catch (IOException e) {
                    return null;
                }
            }
            if(ex.getMessage().contains(App.getContext().getResources().getString(R.string.drawingLockedMessage))){
                return App.getContext().getResources().getString(R.string.drawingLockedMessage);
            }
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