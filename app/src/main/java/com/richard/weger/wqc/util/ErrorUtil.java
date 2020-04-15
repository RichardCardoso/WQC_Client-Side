package com.richard.weger.wqc.util;

import com.google.android.gms.common.util.Strings;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.result.ErrorResult;

import static com.richard.weger.wqc.util.App.getStringResource;

public class ErrorUtil {
    public static String getUnknownErrorMessage(){
        String desc;
        desc = getStringResource(R.string.errorMsgMalformedRequest);
//        desc = desc.concat(" ").concat(App.getStringResource(R.string.tryAgainMessage));
        return desc;
    }

    public static String getErrorMessageWithCode(ErrorResult err){
        String description, code;
        description = err.getDescription();
        code = err.getCode();
        if(!Strings.isEmptyOrWhitespace(description) && !Strings.isEmptyOrWhitespace(code)) {
            return description + " - " + code;
        } else if (!Strings.isEmptyOrWhitespace(err.getRequestCode())){
            return "An unexpected error has occurred while trying to retrieve data from server with request code '" + err.getRequestCode() + "'";
        } else {
            return getStringResource(R.string.unknownErrorMessage);
        }
    }
}
