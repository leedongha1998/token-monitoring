package com.dongha.monitoring.rollup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "daily_rollup",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_daily_rollup_project_date_model",
            columnNames = {"project_id", "date", "model"}))
public class DailyRollup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false, length = 100)
  private String model;

  @Column(name = "total_input_tokens", nullable = false)
  private long totalInputTokens;

  @Column(name = "total_output_tokens", nullable = false)
  private long totalOutputTokens;

  @Column(name = "total_cost", nullable = false, precision = 20, scale = 8)
  private BigDecimal totalCost;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected DailyRollup() {}

  public static DailyRollup create(
      Long projectId,
      LocalDate date,
      String model,
      long totalInputTokens,
      long totalOutputTokens,
      BigDecimal totalCost) {
    DailyRollup rollup = new DailyRollup();
    rollup.projectId = projectId;
    rollup.date = date;
    rollup.model = model;
    rollup.totalInputTokens = totalInputTokens;
    rollup.totalOutputTokens = totalOutputTokens;
    rollup.totalCost = totalCost;
    rollup.createdAt = Instant.now();
    return rollup;
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public LocalDate getDate() {
    return date;
  }

  public String getModel() {
    return model;
  }

  public long getTotalInputTokens() {
    return totalInputTokens;
  }

  public long getTotalOutputTokens() {
    return totalOutputTokens;
  }

  public BigDecimal getTotalCost() {
    return totalCost;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
