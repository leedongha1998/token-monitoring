package com.dongha.monitoring.project.service;

import com.dongha.monitoring.project.domain.ProjectBudget;
import java.math.BigDecimal;

public record ProjectBudgetResult(
    Long id, Long projectId, String yearMonth, BigDecimal monthlyBudgetUsd) {

  public static ProjectBudgetResult from(ProjectBudget budget) {
    return new ProjectBudgetResult(
        budget.getId(), budget.getProjectId(), budget.getYearMonth(), budget.getMonthlyBudgetUsd());
  }
}
