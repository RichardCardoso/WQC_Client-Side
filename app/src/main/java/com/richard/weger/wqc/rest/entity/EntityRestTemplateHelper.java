package com.richard.weger.wqc.rest.entity;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.MultipleObjectResult;
import com.richard.weger.wqc.result.ResourceLocationResult;
import com.richard.weger.wqc.result.SingleObjectResult;
import com.richard.weger.wqc.util.App;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.DELETE_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.GET_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.POST_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_MARKREMOVE_KEY;


public class EntityRestTemplateHelper<T extends DomainEntity> extends RestTemplateHelper<EntityRequest<T>> {

    public EntityRestTemplateHelper(RestResponseHandler delegate, boolean toggleControlsOnCompletion){
        super(delegate, toggleControlsOnCompletion);
    }

    @Override
    protected final AbstractResult executionStrategy(RestTemplate restTemplate, EntityRequest<T> request) {

        AbstractResult result = null;
        String appVersion = null;

        switch (request.getRequestMethod()) {
            case GET_METHOD:
                if (request.getEntityReturnType() == EntityReturnType.EntityListReturn) {
                    ParameterizedTypeReference<List<T>> type = new ParameterizedTypeReference<List<T>>() {
                    };
                    ResponseEntity<List<T>> response = getResponseEntity(type, request.getEntity(), request.getUri(), HttpMethod.GET, restTemplate);
                    result = new MultipleObjectResult<>(request.getClazz(), response.getBody());
                    appVersion = response.getHeaders().getFirst("version");
                } else if (request.getEntityReturnType() == EntityReturnType.SingleEntityReturn) {
                    ResponseEntity<T> response = getResponseEntity(request.getClazz(), request.getEntity(), request.getUri(), HttpMethod.GET, restTemplate);
                    result = new SingleObjectResult<>(request.getClazz(), response.getBody());
                    appVersion = response.getHeaders().getFirst("version");
                }
                if (appVersion == null || !appVersion.equals(App.getExpectedVersion())){
                    result = new ErrorResult(ErrorResult.ErrorCode.INVALID_APP_VERSION, App.getContext().getResources().getString(R.string.invalidVersionMessage), ErrorResult.ErrorLevel.SEVERE, getClass());
                }
                break;
            case POST_METHOD:
//                ResponseEntity<T> response = getResponseEntity(request.getClazz(), request.getEntity(), request.getUri(), HttpMethod.POST, restTemplate);
//                if (response.getBody() != null) {
//                    result = new SingleObjectResult<>(request.getClazz(), response.getBody());
//                } else {
//                    String location = response.getHeaders().getFirst("location");
//                    if (location != null) {
//                        result = new ResourceLocationResult(response.getHeaders().getFirst("location"));
//                    }
//                }
                URI uri;
                uri = getLocation(request.getEntity(), request.getUri(), restTemplate);
                if(uri != null){
                    result = new ResourceLocationResult(uri.toString());
                } else {
                    result = new EmptyResult();
                }
                break;
            case DELETE_METHOD:
                if (requestCode.equals(REST_MARKREMOVE_KEY)) {
                    restTemplate.delete(request.getUri());
                    result = new EmptyResult();
                }
                break;
        }
        return result;
    }

}