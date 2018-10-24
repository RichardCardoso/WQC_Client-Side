package com.richard.weger.wqc.rest;

import com.richard.weger.wqc.util.Configurations;
import com.richard.weger.wqc.util.ConfigurationsManager;
import com.richard.weger.wqc.util.DeviceManager;
import com.richard.weger.wqc.util.StringHandler;

import org.springframework.web.util.UriComponentsBuilder;

import static com.richard.weger.wqc.util.AppConstants.*;

public class UriHelper {

    public void proccess(UriBuilder uriBuilder, String externalUrl){
        Configurations conf = ConfigurationsManager.getLocalConfig();
        String url = "http://" + conf.getServerPath() + "/WQC-2.0/";
        switch(uriBuilder.getRequestCode()){
            case REST_CONFIGLOAD_KEY:
                url += "rest/configurations";
                uriBuilder.setRequestMethod(GET_METHOD);
                break;
            case REST_QRPROJECTLOAD_KEY:
                url += "rest/scan/projects/" + uriBuilder.getParameters().get(0).replace("\\","");
                uriBuilder.setRequestMethod(GET_METHOD);
                break;
            case REST_QRPROJECTCREATE_KEY:
                url += "rest/scan/projects/" + uriBuilder.getParameters().get(0);
                uriBuilder.setRequestMethod(POST_METHOD);
                break;
            case REST_PDFREPORTREQUEST_KEY:
                url += "rest/project/" + uriBuilder.getProject().getId() + "/pdfdocument/" + uriBuilder.getParameters().get(0);
                uriBuilder.setRequestMethod(GET_METHOD);
                break;
            case REST_MARKSAVE_KEY:
                uriBuilder.setRequestMethod(POST_METHOD);
                url += "rest/projects/{pid}/drawings/{did}/reports/{rid}/pages/{pgid}/marks";
                url = url.replace("{pid}", String.valueOf(uriBuilder.getProject().getId()));
                url = url.replace("{did}", String.valueOf(uriBuilder.getProject().getDrawingRefs().get(0).getId()));
                url = url.replace("{rid}", String.valueOf(uriBuilder.getPage().getReport().getId()));
                url = url.replace("{pgid}", String.valueOf((uriBuilder.getPage().getId())));
                break;
            case REST_MARKLOAD_KEY:
                uriBuilder.setRequestMethod(GET_METHOD);
                if(externalUrl == null){
                    url = uriBuilder.getUri().toString();
                }
                break;
            case REST_MARKREMOVE_KEY:
                uriBuilder.setRequestMethod(DELETE_METHOD);
                url += "rest/projects/{pid}/drawings/{did}/reports/{rid}/pages/{pgid}/marks/{mid}";
                url = url.replace("{pid}", String.valueOf(uriBuilder.getProject().getId()));
                url = url.replace("{did}", String.valueOf(uriBuilder.getProject().getDrawingRefs().get(0).getId()));
                url = url.replace("{rid}", String.valueOf(uriBuilder.getPage().getReport().getId()));
                url = url.replace("{pgid}", String.valueOf((uriBuilder.getPage().getId())));
                url = url.replace("{mid}", String.valueOf(uriBuilder.getMark().getId()));
                break;
            case REST_PROJECTSAVE_KEY:
                uriBuilder.setRequestMethod(PUT_METHOD);
                url += "rest/projects"
                + "&deviceId="
                + DeviceManager.getCurrentDevice().getDeviceid();
                break;
            case REST_FIRSTCONNECTIONTEST_KEY:
                uriBuilder.setRequestMethod(GET_METHOD);
                url += "firstConnectionTest";
                break;
            case REST_IDENTIFY_KEY:
                uriBuilder.setRequestMethod(GET_METHOD);
                url += "rest/devices/" + uriBuilder.getParameters().get(0);
                break;
            case REST_ASKAUTHORIZATION_KEY:
                uriBuilder.setRequestMethod(PUT_METHOD);
                url += "rest/devices";
                break;
            case REST_REPORTITEMSSAVE_KEY:
                uriBuilder.setRequestMethod(PUT_METHOD);
                url += "rest/projects/"
                        + uriBuilder.getProject().getId()
                        + "/drawings/"
                        + uriBuilder.getReport().getDrawingref().getId()
                        + "/reports?qrCode="
                        + StringHandler.createQrText(uriBuilder.getProject())
                        + "&deviceId="
                        + DeviceManager.getCurrentDevice().getDeviceid();
                break;
            default:
                uriBuilder.setRequestMethod(null);
                url = null;
        }
        if(externalUrl != null){
            url = externalUrl;
        }
        uriBuilder.setUri(UriComponentsBuilder.fromUriString(url)
                .build()
                .encode()
                .toUri());
    }

    public void execute(UriBuilder uriBuilder){
        proccess(uriBuilder, null);
    }

    public void execute(UriBuilder uriBuilder, String url){
        proccess(uriBuilder, url);
    }
}
