package com.enumeration;

public enum ErrorCode {
	CLASS_NOT_FOUND("100010", "找不到类");
	private final String errorCode;
	private final String errorMsg;

	private ErrorCode(String errorCode, String errorMsg) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
}
