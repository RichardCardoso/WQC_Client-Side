package com.richard.weger.wegerqualitycontrol.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.DRAWING_NUMBER_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PART_NUMBER_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PROJECT_NUMBER_KEY;

public class QrTextHandler extends ConfigurationsDependant{
    private Map<String, String> mapValues = new HashMap<>();

    public QrTextHandler(Context context) {
        super(context);
    }

    @SuppressLint("DefaultLocale")
    public Map<String, String> execute(String qrText){
        // qr_text_sample: \17-1-435_Z_1_T_1
        try {
            StringBuilder sb = new StringBuilder();
            int a, b;
            if(!qrText.startsWith("\\")){
                return null;
            }
            sb.append(conf.getYearPrefix());
            sb.append(qrText.substring(1, 3));
            sb.append("/");
            sb.append(qrText.substring(1, 3));
            sb.append("-");
            a = qrText.indexOf('-');
            b = qrText.indexOf('-', a + 1);
            sb.append(qrText.substring(a + 1, b));
            sb.append("-___/");
            b = qrText.indexOf('Z');
            sb.append(qrText.substring(1, b - 1));
            sb.append("/");

            mapValues.put(AppConstants.COMMON_PATH_KEY, sb.toString());

            sb.append(conf.getConstructionDrawingPath());
            sb.append("Teil");
            a = qrText.indexOf('T');
            sb.append(String.format("%02d", Integer.valueOf(qrText.substring(a + 2, qrText.length()))));
            sb.append("-Z");
            b = qrText.indexOf('Z');
            sb.append(String.format("%02d", Integer.valueOf(qrText.substring(b + 2, a - 1))));

            mapValues.put(AppConstants.CONSTRUCTION_PATH_KEY, sb.toString().concat("/"));

            sb = new StringBuilder();
            sb.append(mapValues.get(AppConstants.COMMON_PATH_KEY).concat(conf.getTechnicDatasheetPath()));
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
        }
        catch (Exception e){
            return null;
        }
    }
}
