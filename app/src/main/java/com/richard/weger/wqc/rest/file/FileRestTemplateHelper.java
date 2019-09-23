package com.richard.weger.wqc.rest.file;

import com.richard.weger.wqc.domain.dto.FileDTO;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.MultipleObjectResult;
import com.richard.weger.wqc.result.SingleObjectResult;
import com.richard.weger.wqc.util.ErrorUtil;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.GET_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.POST_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREUPLOAD_KEY;


public class FileRestTemplateHelper extends RestTemplateHelper<FileRequest> {

    public FileRestTemplateHelper(RestResponseHandler delegate) {
        super(delegate, false);
    }

    @Override
    protected final AbstractResult executionStrategy(RestTemplate restTemplate, FileRequest request) throws Exception {
        AbstractResult result;

        if (request.getRequestMethod().equals(GET_METHOD)) {
            if (request.getFileReturnType() == FileReturnType.ListReturn) {
                ParameterizedTypeReference<List<FileDTO>> type = new ParameterizedTypeReference<List<FileDTO>>() {};
                ResponseEntity<List<FileDTO>> response = getResponseEntity(type, request.getEntity(), request.getUri(), HttpMethod.GET, restTemplate);

                result = new MultipleObjectResult<>(FileDTO.class, response.getBody());

            } else if (request.getFileReturnType() == FileReturnType.PdfReturn || request.getFileReturnType() == FileReturnType.PictureReturn) {
                ResponseEntity<ByteArrayResource> response = getResponseEntity(ByteArrayResource.class, request.getEntity(), request.getUri(), HttpMethod.GET, restTemplate);

                String parentFolder = null;
                if (request.getFileReturnType() == FileReturnType.PdfReturn) {
                    parentFolder = StringHelper.getPdfsFolderName();
                } else if (request.getFileReturnType() == FileReturnType.PictureReturn) {
                    parentFolder = StringHelper.getPicturesFolderName();
                }
                ProjectHelper.byteArrayToFile(response.getBody().getByteArray(), request.getParameters(), parentFolder);

                result = new EmptyResult();
            } else {
                result = new ErrorResult(ErrorResult.ErrorCode.INVALID_ENTITYRETURNTYPE,  ErrorUtil.getUnknownErrorMessage(), ErrorResult.ErrorLevel.SEVERE, EntityRestTemplateHelper.class);
            }
        } else if (request.getRequestMethod().equals(POST_METHOD)) {
            if (requestCode.equals(REST_PICTUREUPLOAD_KEY) || requestCode.equals(REST_GENPICTUREUPLOAD_KEY)) {
                restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());


                ResponseEntity<String> responseEntity;
                responseEntity = getResponseEntity(String.class, request.getEntity(), request.getUri(), HttpMethod.POST, restTemplate);
                if (requestCode.equals(REST_GENPICTUREUPLOAD_KEY) && request.getParameters().stream().anyMatch(p -> p.getName().equals("pictype") && p.getValue().equals("1"))) {
                    String originalFileName = responseEntity.getHeaders().getFirst("originalFileName");
                    String newFileName = responseEntity.getHeaders().getFirst("newFileName");
                    if (newFileName != null && !newFileName.equals(originalFileName)) {
                        String code = request.getParameters().stream()
                                .filter(s -> s.getName().equals("qrcode"))
                                .map(RequestParameter::getValue)
                                .findFirst()
                                .orElse(null);
                        String picFolder = StringHelper.getPicturesFolderPath(code);
                        FileHelper.fileCopy(new File(picFolder.concat(originalFileName)),
                                new File(picFolder.concat(newFileName)));
                        FileHelper.fileDelete(picFolder.concat(originalFileName));
                        request.setRequestCode(REST_PICTUREDOWNLOAD_KEY);
                        request.setRequestMethod(GET_METHOD);
                        RequestParameter param = new RequestParameter();
                        if (request.getParameters().stream().anyMatch(s -> s.getName().equals("filename"))) {
                            param = request.getParameters().stream().filter(s -> s.getName().equals("filename")).findFirst().orElse(new RequestParameter());
                        }
                        if (param.getName() == null || param.getName().isEmpty()) {
                            param.setName("filename");
                        }
                        param.setValue(newFileName);
                        request.getParameters().clear();
                        request.getParameters().add(param);
                        param = new RequestParameter();
                        param.setName("qrcode");
                        param.setValue(code);
                        request.getParameters().add(param);
                        doInBackground(request);

                        result = new SingleObjectResult<>(String.class, newFileName);
                    } else {
                        result = new SingleObjectResult<>(String.class, originalFileName);
                    }
                } else {
                    result = new EmptyResult();
                }
            } else if (requestCode.equals(REST_PICTURESREQUEST_KEY)) {
                ParameterizedTypeReference<List<String>> type = new ParameterizedTypeReference<List<String>>() {
                };
                ResponseEntity<List<String>> responseEntity = getResponseEntity(type, null, request.getUri(), HttpMethod.POST, restTemplate);

                result = new MultipleObjectResult<>(String.class, responseEntity.getBody());
            } else {
                result = new ErrorResult(ErrorResult.ErrorCode.INVALID_REQUESTCODE,  ErrorUtil.getUnknownErrorMessage(), ErrorResult.ErrorLevel.SEVERE, EntityRestTemplateHelper.class);
            }
        } else {
            result = new ErrorResult(ErrorResult.ErrorCode.INVALID_REST_METHOD,  ErrorUtil.getUnknownErrorMessage(), ErrorResult.ErrorLevel.SEVERE, EntityRestTemplateHelper.class);
        }
        return result;
    }

}