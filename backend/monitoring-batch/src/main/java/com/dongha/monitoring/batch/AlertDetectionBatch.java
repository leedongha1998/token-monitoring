package com.dongha.monitoring.batch;

import com.dongha.monitoring.alert.domain.AlertType;
import com.dongha.monitoring.alert.service.AlertService;
import com.dongha.monitoring.project.domain.ProjectBudget;
import com.dongha.monitoring.project.repository.ProjectBudgetQueryPort;
import com.dongha.monitoring.rollup.domain.DailyRollup;
import com.dongha.monitoring.rollup.repository.DailyRollupQueryPort;
import com.dongha.monitoring.usage.domain.SessionEfficiencyResult;
import com.dongha.monitoring.usage.repository.UsageEventQueryPort;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class AlertDetectionBatch {

  private static final double SPIKE_MULTIPLIER = 2.0;
  private static final double OPUS_RATIO_THRESHOLD = 0.7;
  private static final double MULTI_TURN_MULTIPLIER = 3.0;

  private final UsageEventQueryPort usageEventQueryPort;
  private final DailyRollupQueryPort dailyRollupQueryPort;
  private final ProjectBudgetQueryPort projectBudgetQueryPort;
  private final AlertService alertService;

  public AlertDetectionBatch(
      UsageEventQueryPort usageEventQueryPort,
      DailyRollupQueryPort dailyRollupQueryPort,
      ProjectBudgetQueryPort projectBudgetQueryPort,
      AlertService alertService) {
    this.usageEventQueryPort = usageEventQueryPort;
    this.dailyRollupQueryPort = dailyRollupQueryPort;
    this.projectBudgetQueryPort = projectBudgetQueryPort;
    this.alertService = alertService;
  }

  public void detectAlerts(Long projectId, LocalDate date) {
    detectSessionSpike(projectId, date);
    detectInefficientModel(projectId, date);
    detectBudgetBurnRate(projectId, date);
    detectAbnormalMultiTurn(projectId, date);
  }

  private void detectSessionSpike(Long projectId, LocalDate date) {
    Instant dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant sevenDaysAgo = date.minusDays(7).atStartOfDay(ZoneOffset.UTC).toInstant();

    List<SessionEfficiencyResult> todaySessions =
        usageEventQueryPort.findSessionEfficiency(projectId, dayStart, dayEnd);
    List<SessionEfficiencyResult> recentSessions =
        usageEventQueryPort.findSessionEfficiency(projectId, sevenDaysAgo, dayStart);

    if (recentSessions.isEmpty() || todaySessions.isEmpty()) return;

    double avgTokens =
        recentSessions.stream()
            .mapToLong(s -> s.totalInputTokens() + s.totalOutputTokens())
            .average()
            .orElse(0);

    for (SessionEfficiencyResult session : todaySessions) {
      long totalTokens = session.totalInputTokens() + session.totalOutputTokens();
      if (totalTokens > avgTokens * SPIKE_MULTIPLIER) {
        alertService.createAlert(
            projectId,
            AlertType.SESSION_SPIKE,
            String.format(
                "세션 %s의 토큰 사용량(%,d)이 7일 평균(%.0f)의 2배를 초과했습니다",
                session.sessionId(), totalTokens, avgTokens),
            null);
      }
    }
  }

  private void detectInefficientModel(Long projectId, LocalDate date) {
    LocalDate sevenDaysAgo = date.minusDays(7);
    List<DailyRollup> rollups =
        dailyRollupQueryPort.findByProjectIdAndDateBetweenExcludingSyntheticModel(
            projectId, sevenDaysAgo, date);

    if (rollups.isEmpty()) return;

    BigDecimal totalCost =
        rollups.stream().map(DailyRollup::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal opusCost =
        rollups.stream()
            .filter(r -> r.getModel().contains("opus"))
            .map(DailyRollup::getTotalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalCost.compareTo(BigDecimal.ZERO) <= 0) return;

    double opusRatio = opusCost.doubleValue() / totalCost.doubleValue();
    if (opusRatio > OPUS_RATIO_THRESHOLD) {
      alertService.createAlert(
          projectId,
          AlertType.INEFFICIENT_MODEL,
          String.format("최근 7일 비용의 %.0f%%가 Opus 모델에서 발생했습니다. Sonnet 전환을 고려해보세요.", opusRatio * 100),
          null);
    }
  }

  private void detectBudgetBurnRate(Long projectId, LocalDate date) {
    YearMonth yearMonth = YearMonth.from(date);
    Optional<ProjectBudget> budgetOpt =
        projectBudgetQueryPort.findByProjectIdAndYearMonth(projectId, yearMonth.toString());
    if (budgetOpt.isEmpty()) return;

    BigDecimal budget = budgetOpt.get().getMonthlyBudgetUsd();
    LocalDate firstDay = yearMonth.atDay(1);
    List<DailyRollup> rollups =
        dailyRollupQueryPort.findByProjectIdAndDateBetweenExcludingSyntheticModel(
            projectId, firstDay, date);

    BigDecimal usedSoFar =
        rollups.stream().map(DailyRollup::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);

    int dayOfMonth = date.getDayOfMonth();
    int daysInMonth = yearMonth.lengthOfMonth();

    BigDecimal projectedTotal =
        usedSoFar
            .multiply(BigDecimal.valueOf(daysInMonth))
            .divide(BigDecimal.valueOf(dayOfMonth), 4, RoundingMode.HALF_UP);

    if (projectedTotal.compareTo(budget) > 0) {
      alertService.createAlert(
          projectId,
          AlertType.BUDGET_BURN_RATE,
          String.format(
              "현재 소진 속도 기준 이번 달 예상 비용($%.2f)이 예산($%.2f)을 초과할 것으로 예상됩니다.", projectedTotal, budget),
          null);
    }
  }

  private void detectAbnormalMultiTurn(Long projectId, LocalDate date) {
    Instant dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant thirtyDaysAgo = date.minusDays(30).atStartOfDay(ZoneOffset.UTC).toInstant();

    Optional<Double> avgOpt =
        usageEventQueryPort.findAverageInputTokensByProjectAndDateRange(
            projectId, thirtyDaysAgo, dayStart);
    if (avgOpt.isEmpty() || avgOpt.get() <= 0) return;

    long threshold = (long) (avgOpt.get() * MULTI_TURN_MULTIPLIER);
    List<Long> highTokenEventIds =
        usageEventQueryPort.findEventIdsWithHighInputTokens(projectId, dayStart, dayEnd, threshold);

    for (Long eventId : highTokenEventIds) {
      alertService.createAlert(
          projectId,
          AlertType.ABNORMAL_MULTI_TURN,
          String.format(
              "비정상적으로 큰 이벤트(ID: %d)가 감지됐습니다. 입력 토큰이 30일 평균의 3배(임계값: %,d)를 초과했습니다.",
              eventId, threshold),
          null);
    }
  }
}
