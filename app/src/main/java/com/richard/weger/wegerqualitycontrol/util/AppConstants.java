package com.richard.weger.wegerqualitycontrol.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AppConstants {

    public final static String FILE_EXTENSION = "json";
    public final static String CONFIG_FILE_NAME = "configs";
    public final static String CONTROLCARDREPORT_FILENAME = "kontrollkarte.xls";

    public final static int CCRF_FIRSTLINE = 18;
    public final static int CCRF_IO = 12;
    public final static int CCRF_NIO = 13;
    public final static int CCRF_NA = 14;
    public final static int CCRF_COMMENTS = 15;
    public final static List<Integer> CCRF_JUMPONEMORE = new ArrayList<Integer>(){
        { add(36); }
        { add(40); }
    };
    public final static int CCRF_CLIENTCOLUMN = 4;
    public final static int CCRF_CLIENTROW = 5;
    public final static int CCRF_COMMISSIONCOLUMN = 4;
    public final static int CCRF_COMMISSIONROW = 7;
    public final static int CCRF_DRAWINGCOLUMN = 19;
    public final static int CCRF_DRAWINGROW = 5;
    public final static int CCRF_PARTCOLUMN = 19;
    public final static int CCRF_PARTROW = 7;
    public final static int CCRF_SIGNATURECOL = 14;
    public final static int CCRF_SIGNATURELINE = 48;
    public final static int CCRF_REPORTCOMMENTSCOLUMN = 1;
    public final static int CCRF_REPORTCOMMENTSROW = 43;
    public final static int CCRF_REPORTRESPONSIBLECOLUMN = 14;
    public final static int CCRF_REPORTRESPONSIBLEROW = 48;
    public final static int CCRF_REPORTDATECOLUMN = 6;
    public final static int CCRF_REPORTDATEROW = 48;

    public final static String PICTURES_AUTHORITY = "com.richard.weger.wegerqualitycontrol.fileprovider";
    public final static String CONSTRUCTION_PATH_KEY = "constructionPath";
    public final static String TECHNICAL_PATH_KEY = "technicalPath";
    public final static String COMMON_PATH_KEY = "commonPath";
    public final static String PROJECT_NUMBER_KEY = "projectNumber";
    public final static String DRAWING_NUMBER_KEY = "drawingNumber";
    public final static String PART_NUMBER_KEY = "partNumber";
    public final static String ITEM_LIST_KEY = "projectItemList";
    public final static String ITEM_KEY = "projectItem";
    public final static String ITEM_ID_KEY = "itemIdKey";
    public final static String REPORT_KEY = "singleReport";
    public final static String PROJECT_KEY = "singleProject";
    public final static String CAMERA_PERMISSION = "android.permission.CAMERA";
    public final static String EXTERNAL_DIR_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    public final static String DATA_KEY = "data";
    public final static String CLOSE_REASON = "closeReason";
    public final static String DOCUMENT_TYPE_KEY = "documentType";
    public final static String DOCUMENT_KEY = "documentKey";
    public final static String FILE_PATH_KEY = "filePath";
    public final static String DOCUMENT_HASH_POINTS_KEY = "documentHashPoints";
    public final static String MAP_VALUES_KEY = "mapValues";

    public final static int AUTOMATION_COMPONENTS_REPORT_ID = 0;
    public final static int CONTROL_CARD_REPORT_ID = 1;
    public final static int ELETRIC_REPORT_ID = 2;
    public final static int FACTORY_TEST_REPORT_ID = 3;
    public final static int REQUEST_IMAGE_CAPTURE_ACTION = 4;
    public final static int PICTURE_VIEWER_SCREEN_ID = 5;
    public final static int PROJECT_FINISH_SCREEN_ID = 6;
    public final static int SOURCE_SELECTION_SCREEN_KEY = 7;
    public final static int INTRINSIC_PERMISSIONS_CODE = 8;
    public final static int CONTROL_CARD_REPORT_EDIT_SCREEN_KEY = 9;
    public final static int CONTINUE_PROJECT_SCREEN_KEY = 10;
    public final static int CONFIG_SCREEN_KEY = 11;
    public final static int CLOSE_REASON_USER_FINISH = 12;
    public final static int DOCUMENT_MARK_SCREEN = 13;

    public final static String SOURCE_CODE_KEY = "sourceCodeKey";
    public final static String SOURCE_CODE_QR = "sourceCodeQr";
    public final static String SOURCE_CODE_CONTINUE = "sourceCodeContinue";
    public final static String SOURCE_FROMSERVER = "sourceCodeFromServer";
    public final static String CONTROL_CARD_REPORT_FILE_KEY = "controlCardReportFileKey";

    public final static String QR_CODE_KEY = "qr_code_text";
    public final static String CONTINUE_CODE_KEY = "continue_code_text";

    public final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    

    // 0 - not started
    // 1 - approved
    // 2 - reproved
    // 3 - not applicable
    public final static int ITEM_NOT_CHECKED_KEY = 0;
    public final static int ITEM_APROVED_KEY = 1;
    public final static int ITEM_NOT_APROVED_KEY = 2;
    public final static int ITEM_NOT_APLICABLE_KEY = 3;
}
