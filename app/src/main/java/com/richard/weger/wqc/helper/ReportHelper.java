package com.richard.weger.wqc.helper;


import com.richard.weger.wqc.R;
import com.richard.weger.wqc.activity.CheckReportEditActivity;
import com.richard.weger.wqc.activity.ItemReportEditActivity;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.service.FileRequestParametersResolver;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.ConfigurationsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.richard.weger.wqc.appconstants.AppConstants.CHECK_REPORT_EDIT_SCREEN_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_REPORT_EDIT_SCREEN_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PDFREPORTDOWNLOAD_KEY;

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

    public int getTargetActivityKey(Report report){
        if(report instanceof CheckReport){
            return CHECK_REPORT_EDIT_SCREEN_KEY;
        } else if (report instanceof ItemReport){
            return ITEM_REPORT_EDIT_SCREEN_KEY;
        } else {
            return 0;
        }
    }

}
