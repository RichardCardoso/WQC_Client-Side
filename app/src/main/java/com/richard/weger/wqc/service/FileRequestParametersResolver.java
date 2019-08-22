package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileRequest;
import com.richard.weger.wqc.rest.file.FileRequestHelper;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileReturnType;
import com.richard.weger.wqc.rest.file.RawFileRequest;

import static com.richard.weger.wqc.appconstants.AppConstants.GET_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.POST_METHOD;

public class FileRequestParametersResolver {

    RestTemplateHelper.RestTemplateResponse delegate;
    String requestCode;

    public FileRequestParametersResolver(String requestCode, FileRestTemplateHelper.RestTemplateResponse delegate){
        this.delegate = delegate;
        this.requestCode = requestCode;
    }

    public FileRestTemplateHelper getPdf(CheckReport report, String qrcode){
        FileRestTemplateHelper template = new FileRestTemplateHelper(delegate);

        RawFileRequest request = new RawFileRequest();
        request.setRequestCode(requestCode);
        request.setFileReturnType(FileReturnType.PdfReturn);
        request.setRequestMethod(GET_METHOD);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(qrcode);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("filename");
        param.setValue(report.getFileName());
        request.getParameters().add(param);

        FileRequestHelper helper = new FileRequestHelper();
        FileRequest req = helper.proccess(request);

        template.execute(req);

        return template;
    }

    public FileRestTemplateHelper getItemPicturesList(String qrcode){
        FileRestTemplateHelper template = new FileRestTemplateHelper(delegate);

        RawFileRequest request = new RawFileRequest();
        request.setRequestCode(requestCode);
        request.setFileReturnType(FileReturnType.ListReturn);
        request.setRequestMethod(GET_METHOD);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(qrcode);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("pictype");
        param.setValue("0");
        request.getParameters().add(param);

        FileRequestHelper helper = new FileRequestHelper();
        FileRequest req = helper.proccess(request);

        template.execute(req);

        return template;
    }

    public FileRestTemplateHelper getGeneralPicturesList(String qrcode){
        FileRestTemplateHelper template = new FileRestTemplateHelper(delegate);

        RawFileRequest request = new RawFileRequest();
        request.setRequestCode(requestCode);
        request.setFileReturnType(FileReturnType.ListReturn);
        request.setRequestMethod(GET_METHOD);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(qrcode);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("pictype");
        param.setValue("1");
        request.getParameters().add(param);

        FileRequestHelper helper = new FileRequestHelper();
        FileRequest req = helper.proccess(request);

        template.execute(req);

        return template;
    }

    public FileRestTemplateHelper getPicture(String filename, String qrcode){
        FileRestTemplateHelper template = new FileRestTemplateHelper(delegate);

        RawFileRequest request = new RawFileRequest();
        request.setRequestCode(requestCode);
        request.setFileReturnType(FileReturnType.PictureReturn);
        request.setRequestMethod(GET_METHOD);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(qrcode);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("filename");
        param.setValue(filename);
        request.getParameters().add(param);

        FileRequestHelper helper = new FileRequestHelper();
        FileRequest req = helper.proccess(request);

        template.execute(req);

        return template;
    }

    public FileRestTemplateHelper uploadItemPicture(String filename, String qrcode, Long itemid){
        FileRestTemplateHelper template = new FileRestTemplateHelper(delegate);

        RawFileRequest request = new RawFileRequest();
        request.setRequestCode(requestCode);
        request.setRequestMethod(POST_METHOD);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(qrcode);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("filename");
        param.setValue(filename);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("pictype");
        param.setValue("0");
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("id");
        param.setValue(String.valueOf(itemid));
        request.getParameters().add(param);

        FileRequestHelper helper = new FileRequestHelper();
        FileRequest req = helper.proccess(request);

        template.execute(req);

        return template;
    }

    public FileRestTemplateHelper uploadGeneralPicture(String filename, String qrcode){
        FileRestTemplateHelper template = new FileRestTemplateHelper(delegate);

        RawFileRequest request = new RawFileRequest();
        request.setRequestCode(requestCode);
        request.setRequestMethod(POST_METHOD);

        RequestParameter param = new RequestParameter();
        param.setName("qrcode");
        param.setValue(qrcode);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("filename");
        param.setValue(filename);
        request.getParameters().add(param);

        param = new RequestParameter();
        param.setName("pictype");
        param.setValue("1");
        request.getParameters().add(param);

        FileRequestHelper helper = new FileRequestHelper();
        FileRequest req = helper.proccess(request);

        template.execute(req);

        return template;
    }

}
