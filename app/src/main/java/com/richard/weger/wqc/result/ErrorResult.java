package com.richard.weger.wqc.result;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.LoggerManager;

import java.util.logging.Logger;

public class ErrorResult extends AbstractResult {
	
	public enum ErrorLevel {
		SEVERE,
		WARNING,
		LOG
	}
	
	public enum ErrorCode {

		CLIENT_FILE_ACCESS_EXCEPTION,
		CLIENT_INVALID_PICTURE_NAME_EXCEPTION,
		CLIENT_INTENT_DATA_RETRIEVAL_EXCEPTION,
		CLIENT_DEVICE_NOT_AUTHORIZED_EXCEPTION,
		CLIENT_PROJECT_CREATION_REQUEST_EXCEPTION,
		CLIENT_INVALID_ENTITY_EXCEPTION,
		CLIENT_INVALID_ITEM_ID_EXCEPTION,
		CLIENT_REPORT_PAGE_LOAD_EXCEPTION,
		CLIENT_PHOTO_CAPTURE_SETTING_EXCEPTION,

		CLIENT_UNFINISHED_ITEMREPORT_WARNING,
		CLIENT_UNFINISHED_CHECKREPORT_WARNING,
		CLIENT_EMPTY_FIELDS_WARNING,
		CLIENT_MUST_WAIT_WARNING,
		CLIENT_PERMISSIONS_GRANT_WARNING,
		CLIENT_ACCESS_DISABLED_WARNING,
		CLIENT_EMPTY_ROLES_LIST_WARNING,
		CLIENT_INVALID_USERNAME_WARNING,
		CLIENT_SERVER_CONNECTION_FAILED_WARNING,
		CLIENT_UPDATE_SERVICE_CONNECTION_FAILED,

		INVALID_QRCODE,
		INVALID_ENTITY_ID,
		INVALID_ENTITY_VERSION,
		INVALID_REPORT_TYPE,
		INVALID_FILE_CONTENT,
		INVALID_ROLE_DESCRIPTION,
		INVALID_ENTITY_NAME,
		INVALID_ENTITY,
		INVALID_PICTURE_TYPE,
		UNMET_CONDITIONS,
		ENTITY_NOT_FOUND,
		ENTITY_CREATION_FAILED,
		ENTITY_PERSIST_FAILED,
		ENTITY_DELETE_FAILED,
		ENTITY_EXPORT_FAILED,	
		ENTITY_RETRIEVAL_FAILED,
		REPORTS_FOLDER_PATH_RETRIEVAL_FAILED,
		NULL_ENTITY_RECEIVED,
		INVALID_LOCATION_RECEIVED,
		FILENAME_RETRIEVAL_FAILED,
		FILE_PREVIEW_FAILED,
		QR_TRANSLATION_FAILED,
		WRITE_OPERATION_FAILED,
		FILE_UPLOAD_FAILED,
		FILE_DOWNLOAD_PREPARATION_FAILED,
		STALE_ENTITY,
		BASE_FILE_RETRIEVAL_FAILED,
		BASE_FILE_IO_FAILED,
		REST_OPERATION_ERROR,
		UNKNOWN_ERROR,
		INVALID_APP_VERSION,
		GENERAL_SERVER_FAILURE,
		REST_POST_EXECUTOR_ERROR,

		INVALID_REST_METHOD,
		INVALID_ENTITYRETURNTYPE,
		INVALID_REQUESTCODE
	}
	
	private String code;
	private String description;
	private ErrorLevel level;

	public ErrorLevel getLevel() {
		return level;
	}

	private void setLevel(ErrorLevel level) {
		this.level = level;
	}

	public ErrorResult(ErrorCode code, String description, ErrorLevel level) {

		if(description == null){
			description = App.getContext().getResources().getString(R.string.unknownErrorMessage);
		}
		if(code != null) {
			setCode(code.toString());
		} else {
			setCode(ErrorCode.UNKNOWN_ERROR.toString());
		}
		setDescription(description);
		setLevel(level);

	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	private void setDescription(String description) {
		this.description = description;
	}
	
}
