package com.richard.weger.wqc.rest.file;

import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.util.Collections;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.GET_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.POST_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PDFREPORTDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREUPLOAD_KEY;

public class FileRequestHelper {

    public FileRequest proccess(RawFileRequest rawFileRequest) {
        Configurations conf = ConfigurationsManager.getLocalConfig();
        FileRequest request = new FileRequest();
        String url = "http://" + conf.getServerPath() + "/";// + "/WQC-2.0/";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity entity = null;

        switch (rawFileRequest.getRequestMethod()) {
            case GET_METHOD:
                switch (rawFileRequest.getRequestCode()) {
                    case REST_PDFREPORTDOWNLOAD_KEY:
                        url += "rest/pdfdocument";
                        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
                        request.setFileReturnType(FileReturnType.PdfReturn);
                        break;
                    case REST_PICTUREDOWNLOAD_KEY:
                    case REST_GENPICTUREDOWNLOAD_KEY:
                        url += "rest/picture";
                        headers.setAccept(Collections.singletonList(MediaType.IMAGE_JPEG));
                        request.setFileReturnType(FileReturnType.PictureReturn);
                        break;
                    case REST_PICTURESREQUEST_KEY:
                    case REST_GENPICTURESREQUEST_KEY:
                        url += "rest/pictures";
                        request.setFileReturnType(FileReturnType.StringlistReturn);
                        break;
                }
                entity = new HttpEntity<>(headers);
                break;
            case POST_METHOD:
                switch (rawFileRequest.getRequestCode()) {
                    case REST_PICTUREUPLOAD_KEY:
                    case REST_GENPICTUREUPLOAD_KEY:

                        LinkedMultiValueMap params = new LinkedMultiValueMap();
                        String fileName = rawFileRequest.getParameters().stream()
                                .filter(s -> s.getName().equals("filename"))
                                .map(RequestParameter::getValue)
                                .findFirst()
                                .orElse(null);
                        RequestParameter codeParam = rawFileRequest.getParameters().stream().filter(p -> p.getName().equals("qrcode")).findFirst().orElse(null);
                        if (codeParam != null && fileName != null) {
                            String code = codeParam.getValue();
                            fileName = StringHelper.getPicturesFolderPath(code).concat("/").concat(fileName);
                            params.add("file", new FileSystemResource(new File(fileName)));
                            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                            entity = new HttpEntity(params, headers);
                        }

                        url += "rest/picture";
                        break;
                }
        }

        String params = rawFileRequest.getParameters().stream()
                .map(p -> p.getName() + "=" + p.getValue().replace("\\", "").replace("/", ""))
                .collect(Collectors.joining("&"));
        if (params != null) {
            url += "?" + params;
        }
        request.setParameters(rawFileRequest.getParameters());
        request.setRequestCode(rawFileRequest.getRequestCode());
        request.setRequestMethod(rawFileRequest.getRequestMethod());

        if (entity != null) {
            request.setEntity(entity);
        }

        url = url.replace("//", "/");
        request.setUri(UriComponentsBuilder.fromUriString(url)
                .build()
                .encode()
                .toUri());

        return request;

    }
}
