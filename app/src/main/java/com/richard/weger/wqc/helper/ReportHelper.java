package com.richard.weger.wqc.helper;

import android.os.AsyncTask;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.activity.ItemReportEditActivity;
import com.richard.weger.wqc.activity.CheckReportEditActivity;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.ConfigurationsManager;

import java.util.List;
import java.util.logging.FileHandler;

import static com.richard.weger.wqc.constants.AppConstants.REST_PDFREPORTREQUEST_KEY;
import static com.richard.weger.wqc.constants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.constants.AppConstants.REST_PICTURESREQUEST_KEY;

public class ReportHelper {

    public String getReportLabel(String sCode){
        ParamConfigurations conf = ConfigurationsManager.getServerConfig();
        if(sCode.equals(conf.getConstructionDrawingCode())){
            return App.getContext().getResources().getString(R.string.constructionDrawingLabel);
        } else if(sCode.equals(conf.getElectricDrawingCode())){
            return App.getContext().getResources().getString(R.string.electricDrawingLabel);
        } else if(sCode.equals(conf.getDatasheetCode())){
            return App.getContext().getResources().getString(R.string.datasheetLabel);
        } else if(sCode.equals(conf.getControlCardReportCode())){
            return App.getContext().getResources().getString(R.string.controlCardLabel);
        } else {
            return null;
        }
    }

    public Class getTargetActivityClass(Report report){
        if(report instanceof CheckReport){
            return CheckReportEditActivity.class;
        } else if (report instanceof ItemReport){
            return ItemReportEditActivity.class;
        } else {
            return null;
        }
    }

    public void getPdfFilesPath(CheckReport r, RestTemplateHelper restTemplateHelper){
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PDFREPORTREQUEST_KEY);
        uriBuilder.setProject(r.getDrawingref().getProject());
        uriBuilder.getParameters().add(r.getFileName());
        restTemplateHelper.execute(uriBuilder);
    }

    public void requestPictures(RestTemplateHelper.RestHelperResponse delegate, List<Item> items, Project project){
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PICTURESREQUEST_KEY);
        uriBuilder.setMissingPictures(items);
        uriBuilder.setProject(project);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
        restTemplateHelper.execute(uriBuilder);
    }

    public void getPictures(RestTemplateHelper.RestHelperResponse delegate, List<Item> items, List<RestTemplateHelper> restTemplateHelperQueue){
        for(Item item : items) {
            String fileName = item.getPicture().getFileName();
//            boolean exists = FileHelper.isValidFile(StringHelper.getQrText(item.getItemReport().getDrawingref().getProject()).concat("/").concat(fileName));
            if(fileName.length() > 0) {
                RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
                if(restTemplateHelperQueue != null) {
                    restTemplateHelperQueue.add(restTemplateHelper);
                }

                if (fileName.contains("/")) {
                    fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                }

                UriBuilder uriBuilder = new UriBuilder();
                uriBuilder.setRequestCode(REST_PICTUREDOWNLOAD_KEY);
                uriBuilder.setItem(item);
                uriBuilder.setProject(item.getItemReport().getDrawingref().getProject());
                uriBuilder.getParameters().add(fileName);

                restTemplateHelper.execute(uriBuilder);
            }
        }
    }

    public static boolean hasPendingTasks(List<RestTemplateHelper> restTemplateHelperQueue, boolean ignoreLast){
        int limit = 0;

        if(ignoreLast)
            limit = 1;

        if(restTemplateHelperQueue != null){
            removeFinishedTasks(restTemplateHelperQueue);
            return restTemplateHelperQueue.size() > limit;
        } else {
            return false;
        }
    }

    private static void removeFinishedTasks(List<RestTemplateHelper> restTemplateHelperQueue){
        for (int i = 0; i < restTemplateHelperQueue.size(); i ++) {
            RestTemplateHelper r = restTemplateHelperQueue.get(i);
            if (r.getStatus() == AsyncTask.Status.FINISHED || r.isCancelled() || r.getStatus() == AsyncTask.Status.PENDING) {
                restTemplateHelperQueue.remove(r);
            }
        }
    }
}
