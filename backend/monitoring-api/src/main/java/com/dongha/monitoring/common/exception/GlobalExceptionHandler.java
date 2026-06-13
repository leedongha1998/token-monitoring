package com.dongha.monitoring.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    HttpStatus status = toHttpStatus(e.getErrorCode());
    return ResponseEntity.status(status).body(ErrorResponse.of(e.getErrorCode()));
  }

  private HttpStatus toHttpStatus(ErrorCode errorCode) {
    return switch (errorCode) {
      case PROJECT_NOT_FOUND, API_KEY_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case AUTH_INVALID -> HttpStatus.UNAUTHORIZED;
      case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
      case BATCH_SIZE_EXCEEDED -> HttpStatus.PAYLOAD_TOO_LARGE;
    };
  }
}
