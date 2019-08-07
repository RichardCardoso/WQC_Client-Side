package com.richard.weger.wqc.rest.file;

import android.app.Activity;
import android.os.AsyncTask;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.rest.RequestParameter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.GET_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.POST_METHOD;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.helper.LogHelper.writeData;


public class FileRestTemplateHelper extends AsyncTask<FileRequest, Void, FileRestResult> {

    public interface FileRestResponse {
        void FileRestCallback(FileRestResult result);

        void toggleControls(boolean resume);

        void runOnUiThread(Runnable runnable);

        void onError();
    }

    private FileRestResponse delegate;
    private String requestCode;

    public FileRestTemplateHelper(FileRestResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected FileRestResult doInBackground(FileRequest... req) {
        RestTemplate restTemplate = new RestTemplate();
        FileRequest request = req[0];
        FileRestResult result = new FileRestResult();

        try {
            delegate.runOnUiThread(() -> delegate.toggleControls(false));
        } catch (Exception ignored) {
        }

        requestCode = request.getRequestCode();
        result.setRequestCode(requestCode);

        try {
            if (request.getRequestMethod().equals(GET_METHOD)) {
                if (request.getFileReturnType() == FileReturnType.StringlistReturn) {
                    ParameterizedTypeReference<List<String>> type = new ParameterizedTypeReference<List<String>>() {
                    };
                    ResponseEntity<List<String>> response = getResponseEntity(type, request.getEntity(), request.getUri(), HttpMethod.GET, restTemplate);

                    result.setContent(null);
                    result.setExistingContent(response.getBody());
                    result.setStatus(response.getStatusCode());
                    result.setMessage(response.getHeaders().getFirst("message"));

                } else if (request.getFileReturnType() == FileReturnType.PdfReturn || request.getFileReturnType() == FileReturnType.PictureReturn) {
                    ResponseEntity<byte[]> response = getResponseEntity(byte[].class, request.getEntity(), request.getUri(), HttpMethod.GET, restTemplate);

                    String parentFolder = null;
                    if (request.getFileReturnType() == FileReturnType.PdfReturn) {
                        parentFolder = StringHelper.getPdfsFolderName();
                    } else if (request.getFileReturnType() == FileReturnType.PictureReturn) {
                        parentFolder = StringHelper.getPicturesFolderName();
                    }
                    ProjectHelper.byteArrayToFile(response.getBody(), request.getParameters(), parentFolder);

                    result.setStatus(response.getStatusCode());
                    result.setMessage(response.getHeaders().getFirst("message"));
                }
            } else if (request.getRequestMethod().equals(POST_METHOD)) {
                if (requestCode.equals(REST_PICTUREUPLOAD_KEY) || requestCode.equals(REST_GENPICTUREUPLOAD_KEY)) {
                    restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

                    ResponseEntity<String> responseEntity;
                    responseEntity = getResponseEntity(String.class, request.getEntity(), request.getUri(), HttpMethod.POST, restTemplate);
                    if (responseEntity.getStatusCode() == HttpStatus.OK) {
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
                                request.getParameters().clear();
                                RequestParameter param = new RequestParameter();
                                if (request.getParameters().stream().anyMatch(s -> s.getName().equals("filename"))) {
                                    param = request.getParameters().stream().filter(s -> s.getName().equals("filename")).findFirst().orElse(new RequestParameter());
                                }
                                if (param.getName() == null || param.getName().isEmpty()) {
                                    param.setName("filename");
                                }
                                param.setValue(newFileName);
                                doInBackground(request);

                                result.setMessage(newFileName);
                            } else {
                                result.setMessage(originalFileName);
                            }
                        }
                        result.setStatus(responseEntity.getStatusCode());
                    }
                } else if (requestCode.equals(REST_PICTURESREQUEST_KEY)) {
                    ParameterizedTypeReference<List<String>> type = new ParameterizedTypeReference<List<String>>() {
                    };
                    ResponseEntity<List<String>> responseEntity = getResponseEntity(type, null, request.getUri(), HttpMethod.POST, restTemplate);

                    if (responseEntity.getStatusCode() == HttpStatus.OK) {
                        result.setExistingContent(responseEntity.getBody());
                    }
                    result.setStatus(responseEntity.getStatusCode());
                }
            }
        } catch (Exception ex) {
            try{
                delegate.onError();
            } catch (Exception ignored){}

            printStackTrace(ex);
            result.setMessage(((Activity) delegate).getResources().getString(R.string.unknownErrorMessage));
            result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    private void printStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sStackTrace = sw.toString();
        writeData(sStackTrace);
    }

    private <E, S> ResponseEntity<S> getResponseEntity(Class<S> responseClazz, HttpEntity<E> entity, URI uri, HttpMethod method, RestTemplate restTemplate) {
        String url = "unknown";
        try{
            url = uri.toURL().toString();
        } catch (Exception ignored){}
        writeData("Started 'get' (exchange) request (code: " + requestCode + ", url:" + url + ") ");
        ResponseEntity<S> responseEntity = restTemplate.exchange(uri, method, entity, responseClazz);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK) && !responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            writeData("Request resulted in error code " + responseEntity.getStatusCode());
        }
        return responseEntity;
    }

    private <E, S> ResponseEntity<S> getResponseEntity(ParameterizedTypeReference<S> responseType, HttpEntity<E> entity, URI uri, HttpMethod method, RestTemplate restTemplate) {
        String url = "unknown";
        try{
            url = uri.toURL().toString();
        } catch (Exception ignored){}
        writeData("Started 'get' (exchange) request (code: " + requestCode + ", url:" + url + ") ");
        ResponseEntity<S> responseEntity = restTemplate.exchange(uri, method, entity, responseType);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK) && !responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            writeData("Request resulted in error code " + responseEntity.getStatusCode());
        }
        return responseEntity;
    }

    @Override
    protected void onPostExecute(FileRestResult result) {
        delegate.FileRestCallback(result);
    }

}