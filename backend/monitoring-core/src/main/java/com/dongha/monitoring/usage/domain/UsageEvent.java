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
      Instant occurredAt,
      String rawPayload) {
    UsageEvent event = new UsageEvent();
    event.projectId = projectId;
    event.idempotencyKey = idempotencyKey;
    event.model = model;
    event.inputTokens = inputTokens;
    event.outputTokens = outputTokens;
    event.occurredAt = occurredAt;
    event.rawPayload = rawPayload;
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

  public void fillRawPayload(String newRawPayload) {
    if (this.rawPayload != null) return;
    this.rawPayload = newRawPayload;
  }

  public String getPromptSummary() {
    if (rawPayload == null || rawPayload.isBlank()) return null;
    // PostgreSQL JSONB normalizes JSON by adding a space after colons; try both forms.
    String key = "\"promptSummary\": \"";
    int start = rawPayload.indexOf(key);
    if (start == -1) {
      key = "\"promptSummary\":\"";
      start = rawPayload.indexOf(key);
    }
    if (start == -1) return null;
    start += key.length();
    StringBuilder sb = new StringBuilder();
    boolean escaped = false;
    for (int i = start; i < rawPayload.length(); i++) {
      char c = rawPayload.charAt(i);
      if (escaped) {
        switch (c) {
          case '"' -> sb.append('"');
          case '\\' -> sb.append('\\');
          case 'n' -> sb.append('\n');
          case 'r' -> sb.append('\r');
          case 't' -> sb.append('\t');
          default -> sb.append(c);
        }
        escaped = false;
      } else if (c == '\\') {
        escaped = true;
      } else if (c == '"') {
        break;
      } else {
        sb.append(c);
      }
    }
    return sb.isEmpty() ? null : sb.toString();
  }
}
