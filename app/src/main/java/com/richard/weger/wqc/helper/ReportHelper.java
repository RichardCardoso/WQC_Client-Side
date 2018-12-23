package com.richard.weger.wqc.helper;


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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.richard.weger.wqc.constants.AppConstants.REST_PDFREPORTREQUEST_KEY;
import static com.richard.weger.wqc.constants.AppConstants.REST_PICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.constants.AppConstants.REST_PICTURESREQUEST_KEY;

public class ReportHelper {

    private Map<String, String> mapReportLabel;
    public ReportHelper(){
        ParamConfigurations conf = ConfigurationsManager.getServerConfig();

        mapReportLabel = new HashMap<>();
        mapReportLabel.put(conf.getControlCardReportCode(), App.getContext().getResources().getString(R.string.controlCardLabel));
        mapReportLabel.put(conf.getWiredDatasheetCode(), App.getContext().getResources().getString(R.string.wiredDatasheetLabel));
        mapReportLabel.put(conf.getCablelessDatasheetCode(), App.getContext().getResources().getString(R.string.cablelessDatasheetLabel));
        mapReportLabel.put(conf.getWiredDrawingCode(), App.getContext().getResources().getString(R.string.wiredDrawingLabel));
        mapReportLabel.put(conf.getCablelessDrawingCode(), App.getContext().getResources().getString(R.string.cablelessDrawingLabel));
    }

    public String getReportLabel(String sCode){
        if(mapReportLabel.containsKey(sCode)){
            return mapReportLabel.get(sCode);
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

    public void getPictures(RestTemplateHelper.RestHelperResponse delegate, List<Item> items, List<RestTemplateHelper> restTemplateHelperQueue) {
        for (Item item : items) {
            String fileName = item.getPicture().getFileName();
//            boolean exists = FileHelper.isValidFile(StringHelper.getQrText(item.getItemReport().getDrawingref().getProject()).concat("/").concat(fileName));
            if (fileName.length() > 0) {
                RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
                if (restTemplateHelperQueue != null) {
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

}
