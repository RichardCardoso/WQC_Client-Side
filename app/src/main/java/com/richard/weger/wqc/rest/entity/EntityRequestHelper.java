package com.richard.weger.wqc.rest.entity;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.*;

public class EntityRequestHelper {

    public <T extends DomainEntity> EntityRequest<T> proccess(RawEntityRequest<T> rawEntityRequest, String externalUrl){
        Configurations conf = ConfigurationsManager.getLocalConfig();
        EntityRequest<T> request = new EntityRequest<>();
        String url = "http://" + conf.getServerPath() + "/";// + "/WQC-2.0/";

        if(externalUrl != null){
            request.setRequestMethod(GET_METHOD);
            request.setEntityReturnType(EntityReturnType.SingleEntityReturn);
            url = externalUrl;
        } else {
            T e = rawEntityRequest.getSingleEntity();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<T> entity = null;

            switch (rawEntityRequest.getRequestMethod()) {
                case GET_METHOD:
                    entity = new HttpEntity<>(headers);
                    if(rawEntityRequest.getEntityReturnType() == EntityReturnType.SingleEntityReturn) {
                        url += "rest/" + e.getClass().getSimpleName() + "/" + e.getId();
                    } else {
                        url += "rest/" + e.getClass().getSimpleName();
                    }
                    request.setEntityReturnType(rawEntityRequest.getEntityReturnType());
                    break;
                case POST_METHOD:
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    entity = new HttpEntity<>(e, headers);
                    url += "rest/" + e.getClass().getSimpleName();
                    break;
                case DELETE_METHOD:
                    url += "rest/" + e.getClass().getSimpleName();
                    break;
            }

            if (rawEntityRequest.getOverriddenResource() != null && rawEntityRequest.getOverriddenResource().length() > 0) {
                url = url.substring(0, url.indexOf("rest/") + "rest/".length());
                url = url.concat(rawEntityRequest.getOverriddenResource());
            }
            String params = rawEntityRequest.getParameters().stream()
                    .map(p -> p.getName() + "=" + p.getValue())
                    .collect(Collectors.joining("&"));
            if (params != null) {
                url += "?" + params;
            }
            if (entity != null) {
                request.setEntity(entity);
            }
            request.setParameters(new ArrayList<>(rawEntityRequest.getParameters()));
            request.setRequestMethod(rawEntityRequest.getRequestMethod());
            request.setClazz((Class<T>) e.getClass());
        }
        if(request.getRequestCode() == null) {
            request.setRequestCode(rawEntityRequest.getRequestCode());
        }
        request.setUri(UriComponentsBuilder.fromUriString(url)
                .build()
                .encode()
                .toUri());

        return request;

    }

    public <T extends DomainEntity>  void execute(EntityRequest<T> request, String url){
        request.setRequestMethod(GET_METHOD);
        request.setEntityReturnType(EntityReturnType.SingleEntityReturn);
        request.setUri(UriComponentsBuilder.fromUriString(url)
                .build()
                .encode()
                .toUri());
    }
}
