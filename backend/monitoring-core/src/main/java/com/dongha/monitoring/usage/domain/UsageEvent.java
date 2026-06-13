package com.dongha.monitoring.usage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "usage_event")
public class UsageEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "idempotency_key", nullable = false, length = 255)
  private String idempotencyKey;

  @Column(nullable = false, length = 100)
  private String model;

  @Column(name = "input_tokens", nullable = false)
  private int inputTokens;

  @Column(name = "output_tokens", nullable = false)
  private int outputTokens;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_payload", columnDefinition = "jsonb")
  private String rawPayload;

  protected UsageEvent() {}

  public static UsageEvent create(
      Long projectId,
      String idempotencyKey,
      String model,
      int inputTokens,
      int outputTokens,
      Instant occurredAt) {
    UsageEvent event = new UsageEvent();
    event.projectId = projectId;
    event.idempotencyKey = idempotencyKey;
    event.model = model;
    event.inputTokens = inputTokens;
    event.outputTokens = outputTokens;
    event.occurredAt = occurredAt;
    return event;
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getModel() {
    return model;
  }

  public int getInputTokens() {
    return inputTokens;
  }

  public int getOutputTokens() {
    return outputTokens;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getRawPayload() {
    return rawPayload;
  }
}
