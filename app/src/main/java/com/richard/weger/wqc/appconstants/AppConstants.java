package com.richard.weger.wqc.appconstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class AppConstants{
	
	protected AppConstants() {}
	
	private int id=1;

	public final static String FILE_EXTENSION = "json";
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

	public final static int CCRF_COMMISSIONCOLUMN = 4;
	public final static int CCRF_COMMISSIONROW = 7;
	public final static int CCRF_DRAWINGCOLUMN = 19;
	public final static int CCRF_DRAWINGROW = 5;
	public final static int CCRF_PARTCOLUMN = 19;
	public final static int CCRF_PARTROW = 7;
	public final static int CCRF_REPORTDATECOLUMN = 6;
	public final static int CCRF_REPORTDATEROW = 48;

	public final static String PICTURES_AUTHORITY = "com.richard.weger.wqc.fileprovider";
	public final static String CONSTRUCTION_PATH_KEY = "constructionPath";
	public final static String TECHNICAL_PATH_KEY = "technicalPath";
	public final static String COMMON_PATH_KEY = "commonPath";
	public final static String PROJECT_NUMBER_KEY = "projectNumber";
	public final static String DRAWING_NUMBER_KEY = "drawingNumber";
	public final static String PART_NUMBER_KEY = "partNumber";
	public final static String ITEM_KEY = "projectItem";
	public final static String ITEM_ID_KEY = "itemIdKey";
	public final static String REPORT_ID_KEY = "itemIdKey";
	public final static String REPORT_KEY = "singleReport";
	public final static String PROJECT_KEY = "singleProject";
	public final static String TAKEN_PICTURES_KEY = "takenPictures";
	public final static String CAMERA_PERMISSION = "android.permission.CAMERA";
	public final static String EXTERNAL_DIR_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
	public final static String DATA_KEY = "data";
	public final static String PICTURE_CAPTURE_MODE = "pictureCaptureMode";
	public final static String ITEM_PICTURE_MODE = "itemPictureMode";
	public final static String GENERAL_PICTURE_MODE = "generalPictureMode";
	public final static String PICTURE_FILEPATH_KEY = "pictureFilenameKey";
	public final static String PARAMCONFIG_KEY = "paramConfigKey";

	public final static int CONFIG_SCREEN_KEY = 0;
	public final static int CONTROL_CARD_REPORT_ID = 1;
	public final static int CHECK_REPORT_EDIT_SCREEN_KEY = 2;
	public final static int PROJECT_EDIT_SCREEN_KEY = 3;
	public final static int REQUEST_IMAGE_CAPTURE_ACTION = 4;
	public final static int PICTURE_VIEWER_SCREEN_ID = 5;
	public final static int PROJECT_FINISH_SCREEN_ID = 6;
	public final static int INTRINSIC_PERMISSIONS_CODE = 7;
	public final static int PICTURE_LIST_SCREEN_ID = 8;
	public final static int ITEM_REPORT_EDIT_SCREEN_KEY = 9;
	public final static int WELCOME_ACTIVITY_SCREEN_KEY = 10;

	public final static String RESTART_KEY = "restartKey";

	public final static String REST_QRPROJECTLOAD_KEY = "httpProjectLoadKey";
	public final static String REST_QRPROJECTCREATE_KEY = "httpProjectCreateKey";
	public final static String REST_CONFIGLOAD_KEY = "httpConfigLoadKey";
	public final static String REST_PDFREPORTDOWNLOAD_KEY = "httpPdfReportRequestKey";
	public final static String REST_PICTURESREQUEST_KEY = "httpPicturesRequestKey";
	public final static String REST_PICTUREDOWNLOAD_KEY = "httpPictureDownloadKey";
	public final static String REST_PICTUREUPLOAD_KEY = "httpPictureUploadKey";
	public final static String REST_GENPICTURESREQUEST_KEY = "httpGenPicturesRequestKey";
	public final static String REST_GENPICTUREDOWNLOAD_KEY = "httpGenPictureDownloadKey";
	public final static String REST_GENPICTUREUPLOAD_KEY = "httpGenPictureUploadKey";
	public final static String REST_PROJECTSAVE_KEY = "httpProjectSaveKey";
	public final static String REST_MARKSAVE_KEY = "httpMarkSaveKey";
	public final static String REST_IDENTIFY_KEY = "httpIdentifyKey";
	public final static String REST_MARKREMOVE_KEY = "httpMarkRemoveKey";
	public final static String REST_MARKLOAD_KEY = "httpMarkLoadKey";
	public final static String REST_ASKAUTHORIZATION_KEY = "httpAskAuthorizationKey";
	public final static String REST_ITEMSAVE_KEY = "httpItemSaveKey";
	public final static String REST_PROJECTUPLOAD_KEY = "httpProjectUploadKey";
	public final static String GET_METHOD = "getMethod";
	public final static String POST_METHOD = "postMethod";
	public final static String DELETE_METHOD = "deleteMethod";
	public final static String REST_REPORTUPLOAD_KEY = "httpReportUpload";

	public final static int REST_PICTURETYPE_KEY = 0;
	public final static int REST_GENPICTURETYPE_KEY = 1;

	public final static String YES_NO = "yesNoMessageboxKey";
	public final static String OK = "okMessageboxKey";

	public final static String CONTINUE_CODE_KEY = "continue_code_text";

	public final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'T'");


	// 0 - not started
	// 1 - approved
	// 2 - reproved
	// 3 - not applicable
	public final static int ITEM_NOT_CHECKED_KEY = 0;
	public final static int ITEM_APROVED_KEY = 1;
	public final static int ITEM_NOT_APROVED_KEY = 2;
	public final static int ITEM_NOT_APLICABLE_KEY = 3;
}
