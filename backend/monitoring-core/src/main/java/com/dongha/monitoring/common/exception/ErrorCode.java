package com.dongha.monitoring.common.exception;

public enum ErrorCode {
  PROJECT_NOT_FOUND("PROJECT-001", "프로젝트를 찾을 수 없습니다"),
  API_KEY_NOT_FOUND("PROJECT-002", "API 키를 찾을 수 없습니다"),
  AUTH_INVALID("AUTH-001", "유효하지 않은 API 키입니다"),
  INVALID_REQUEST("COMMON-001", "요청 값이 유효하지 않습니다"),
  BATCH_SIZE_EXCEEDED("USAGE-001", "배치 최대 건수(100)를 초과했습니다");

  private final String code;
  private final String message;

  ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
