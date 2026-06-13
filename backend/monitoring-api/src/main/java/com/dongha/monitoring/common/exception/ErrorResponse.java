package com.dongha.monitoring.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
    String code, String message, OffsetDateTime timestamp, List<FieldError> errors) {

  public record FieldError(String field, String reason) {}

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(
        errorCode.getCode(), errorCode.getMessage(), OffsetDateTime.now(), List.of());
  }
}
