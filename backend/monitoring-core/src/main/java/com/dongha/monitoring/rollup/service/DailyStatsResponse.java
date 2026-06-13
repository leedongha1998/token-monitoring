package com.dongha.monitoring.rollup.service;

import com.dongha.monitoring.rollup.domain.DailyRollup;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatsResponse(
    Long projectId,
    LocalDate date,
    String model,
    long totalInputTokens,
    long totalOutputTokens,
    BigDecimal totalCost) {

  public static DailyStatsResponse from(DailyRollup rollup) {
    return new DailyStatsResponse(
        rollup.getProjectId(),
        rollup.getDate(),
        rollup.getModel(),
        rollup.getTotalInputTokens(),
        rollup.getTotalOutputTokens(),
        rollup.getTotalCost());
  }
}
