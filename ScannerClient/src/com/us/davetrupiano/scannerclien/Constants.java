package com.us.davetrupiano.scannerclien;

public final class Constants {
	
	private Constants() {
    }
	
	//commands sent to server
	public static final String CMD_OPEN_SESSION = "openSession";
	public static final String CMD_CHECK_SCAN = "scanCheck";
	public static final String CMD_REASSIGN_BIN = "reassignBin";
	public static final String CMD_GET_BIN_DISPLAY = "getBinDisplay";
	public static final String CMD_ADDRESS_MISMATCH = "logScanResponse";
	
	//parameters for commands sent to server
	public static final String PARAM_BIN_ID = "binID";
	public static final String PARAM_SESSION_ID = "sessionID";
	public static final String PARAM_BARCODE = "barcode";
	public static final String PARAM_USER_HASH = "userHash";
	public static final String PARAM_SCAN_EVENT_ID = "scanEventID";
	public static final String PARAM_MISMATCH_RESPONSE_ID = "mismatchResponseID";
	public static final String PARAM_SCAN_HISTORY_POSITION = "clientTransactionID";
	
	//parameters received from server
	public static final String JSON_SESSION_ID = "sessionID";
	public static final String JSON_DRUG_DISPLAY = "drugDisplay";
	public static final String JSON_RXCUI = "RXCUI";
	public static final String JSON_SCAN_SUCCESS = "matchSuccess";
	public static final String JSON_ERROR_MESSAGE = "responseMessage";
	public static final String JSON_RESPONSE_CODE = "responseCode";
	public static final String JSON_REASSIGN_OPTIONS = "assignOptions";
	public static final String JSON_SCANNED_DRUGS = "scannedDrugs";
	public static final String JSON_SCAN_EVENT_ID = "scanEventID";
	public static final String JSON_CLIENT_TRANSACTION_ID = "clientTransactionID";
	
	//status codes for scan match
	public static final int MM_STATUS_ADDRESSED = 0;
	public static final int MM_STATUS_NOT_ADDRESSED = 1;
	public static final int MM_STATUS_NA = 2;
}
