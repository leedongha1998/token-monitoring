package com.dongha.monitoring.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "project_budget")
public class ProjectBudget {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "year_month", nullable = false, length = 7)
  private String yearMonth;

  @Column(name = "monthly_budget_usd", nullable = false, precision = 12, scale = 4)
  private BigDecimal monthlyBudgetUsd;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ProjectBudget() {}

  private ProjectBudget(Long projectId, String yearMonth, BigDecimal monthlyBudgetUsd) {
    this.projectId = projectId;
    this.yearMonth = yearMonth;
    this.monthlyBudgetUsd = monthlyBudgetUsd;
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  public static ProjectBudget create(
      Long projectId, String yearMonth, BigDecimal monthlyBudgetUsd) {
    return new ProjectBudget(projectId, yearMonth, monthlyBudgetUsd);
  }

  public void updateBudget(BigDecimal monthlyBudgetUsd) {
    this.monthlyBudgetUsd = monthlyBudgetUsd;
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public String getYearMonth() {
    return yearMonth;
  }

  public BigDecimal getMonthlyBudgetUsd() {
    return monthlyBudgetUsd;
  }
}
