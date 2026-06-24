package com.dongha.monitoring.alert.service;

import com.dongha.monitoring.alert.domain.Alert;
import com.dongha.monitoring.alert.domain.AlertType;
import com.dongha.monitoring.alert.repository.AlertRepository;
import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AlertService {

  private final AlertRepository alertRepository;

  public AlertService(AlertRepository alertRepository) {
    this.alertRepository = alertRepository;
  }

  @Transactional
  public Alert createAlert(Long projectId, AlertType alertType, String message, String metadata) {
    return alertRepository.save(Alert.create(projectId, alertType, message, metadata));
  }

  public List<AlertResult> findByProject(Long projectId) {
    return alertRepository.findByProjectIdOrderByTriggeredAtDesc(projectId).stream()
        .map(AlertResult::from)
        .toList();
  }

  public long countUnread(Long projectId) {
    return alertRepository.countByProjectIdAndIsReadFalse(projectId);
  }

  @Transactional
  public void markAsRead(Long alertId) {
    Alert alert =
        alertRepository
            .findById(alertId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ALERT_NOT_FOUND));
    alert.markAsRead();
  }
}
