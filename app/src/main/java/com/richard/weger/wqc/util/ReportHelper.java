package com.richard.weger.wqc.util;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.activity.ItemReportEditActivity;
import com.richard.weger.wqc.activity.CheckReportEditActivity;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.paramconfigs.ParamConfigurations;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;

import static com.richard.weger.wqc.util.AppConstants.REST_PDFREPORTREQUEST_KEY;

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

    public void getPdfFilesPath(RestTemplateHelper.HttpHelperResponse delegate, CheckReport r, RestTemplateHelper restTemplateHelper){
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PDFREPORTREQUEST_KEY);
        uriBuilder.setProject(r.getDrawingref().getProject());
        uriBuilder.getParameters().add(r.getServerPdfPath());
        restTemplateHelper.execute(uriBuilder);
    }
}
