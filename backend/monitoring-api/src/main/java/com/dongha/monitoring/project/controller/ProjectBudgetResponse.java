package com.dongha.monitoring.project.controller;

import com.dongha.monitoring.project.service.ProjectBudgetResult;
import java.math.BigDecimal;

public record ProjectBudgetResponse(
    Long id, Long projectId, String yearMonth, BigDecimal monthlyBudgetUsd) {

  public static ProjectBudgetResponse from(ProjectBudgetResult result) {
    return new ProjectBudgetResponse(
        result.id(), result.projectId(), result.yearMonth(), result.monthlyBudgetUsd());
  }
}
