package com.dongha.monitoring.alert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "alert")
public class Alert {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "alert_type", nullable = false, length = 50)
  private AlertType alertType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(name = "triggered_at", nullable = false)
  private Instant triggeredAt;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String metadata;

  protected Alert() {}

  public static Alert create(Long projectId, AlertType alertType, String message, String metadata) {
    Alert alert = new Alert();
    alert.projectId = projectId;
    alert.alertType = alertType;
    alert.message = message;
    alert.triggeredAt = Instant.now();
    alert.isRead = false;
    alert.metadata = metadata;
    return alert;
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public AlertType getAlertType() {
    return alertType;
  }

  public String getMessage() {
    return message;
  }

  public Instant getTriggeredAt() {
    return triggeredAt;
  }

  public boolean isRead() {
    return isRead;
  }

  public String getMetadata() {
    return metadata;
  }

  public void markAsRead() {
    this.isRead = true;
  }
}
