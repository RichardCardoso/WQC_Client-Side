package com.richard.weger.wqc.helper;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.util.LoggerManager;

import java.util.HashMap;
import java.util.Map;

import static com.richard.weger.wqc.appconstants.AppConstants.DRAWING_NUMBER_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PART_NUMBER_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_NUMBER_KEY;
import static com.richard.weger.wqc.util.App.getLocale;

public class QrTextHelper {
    private Map<String, String> mapValues = new HashMap<>();

    private ParamConfigurations conf;

    QrTextHelper(ParamConfigurations conf)
    {
        this.conf = conf;
    }

    public Map<String, String> execute(String qrText){
        // qr_text_sample: \17-1-435_Z_1_T_1
        try {
            StringBuilder sb = new StringBuilder();

            String commonPath;

            int a, b;
            if(!qrText.startsWith("\\")){
                LoggerManager.getLogger(QrTextHelper.class).severe("Qr code does not starts with two slashes");
                return null;
            }
            if(qrText.contains(" ")){
                qrText = qrText.replace(" ", "");
            }
            sb.append(conf.getYearPrefix());
            sb.append(qrText, 1, 3);
            sb.append("/");
            sb.append(qrText, 1, 3);
            sb.append("-");
            a = qrText.indexOf('-');
            b = qrText.indexOf('-', a + 1);
            sb.append(qrText, a + 1, b);
            sb.append("-___/");
            b = qrText.indexOf('Z');
            sb.append(qrText, 1, b - 1);
            sb.append("/");

            commonPath = sb.toString();
            mapValues.put(AppConstants.COMMON_PATH_KEY, commonPath);

            sb.append(conf.getOriginalDocsPath());
            sb.append("Teil");
            a = qrText.indexOf('T');
            sb.append(String.format(getLocale(), "%02d", Integer.valueOf(qrText.substring(a + 2))));
            sb.append("-Z");
            b = qrText.indexOf('Z');
            sb.append(String.format(getLocale(), "%02d", Integer.valueOf(qrText.substring(b + 2, a - 1))));

            mapValues.put(AppConstants.CONSTRUCTION_PATH_KEY, sb.toString().concat("/"));

            sb = new StringBuilder();
            sb.append(commonPath.concat(conf.getOriginalDocsPath()));
            mapValues.put(AppConstants.TECHNICAL_PATH_KEY, sb.toString());

            // qr_text_sample: \17-1-435_Z_1_T_1
            b = qrText.indexOf('Z') - 1;
            mapValues.put(PROJECT_NUMBER_KEY, qrText.substring(1, b));
            a = qrText.indexOf('Z') + 2;
            b = qrText.indexOf('T') - 1;
            mapValues.put(DRAWING_NUMBER_KEY, qrText.substring(a, b));
            a = qrText.indexOf('T') + 2;
            b = qrText.length();
            mapValues.put(PART_NUMBER_KEY, qrText.substring(a, b));

            return mapValues;
        } catch (Exception e){
            LoggerManager.getLogger(QrTextHelper.class).severe(StringHelper.getStackTraceAsString(e));
            return null;
        }
    }
}
