package com.dongha.monitoring.alert.service;

import com.dongha.monitoring.alert.domain.Alert;
import java.time.Instant;

public record AlertResult(
    Long id,
    Long projectId,
    String alertType,
    String message,
    Instant triggeredAt,
    boolean isRead) {

  public static AlertResult from(Alert alert) {
    return new AlertResult(
        alert.getId(),
        alert.getProjectId(),
        alert.getAlertType().name(),
        alert.getMessage(),
        alert.getTriggeredAt(),
        alert.isRead());
  }
}
