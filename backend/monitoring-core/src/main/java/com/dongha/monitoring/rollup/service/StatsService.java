package com.dongha.monitoring.rollup.service;

import com.dongha.monitoring.rollup.domain.DailyRollup;
import com.dongha.monitoring.rollup.repository.DailyRollupRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

  private final DailyRollupRepository dailyRollupRepository;

  public StatsService(DailyRollupRepository dailyRollupRepository) {
    this.dailyRollupRepository = dailyRollupRepository;
  }

  public List<DailyStatsResponse> getDailyStats(
      Long projectId, LocalDate from, LocalDate to, String model) {
    List<DailyRollup> rollups = fetchRollups(projectId, from, to);
    if (model != null) {
      rollups = rollups.stream().filter(r -> model.equals(r.getModel())).toList();
    }
    return rollups.stream().map(DailyStatsResponse::from).toList();
  }

  public SummaryStatsResponse getSummary(Long projectId, LocalDate from, LocalDate to) {
    List<DailyRollup> rollups = fetchRollups(projectId, from, to);
    long totalInputTokens = rollups.stream().mapToLong(DailyRollup::getTotalInputTokens).sum();
    long totalOutputTokens = rollups.stream().mapToLong(DailyRollup::getTotalOutputTokens).sum();
    BigDecimal totalCost =
        rollups.stream().map(DailyRollup::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new SummaryStatsResponse(totalInputTokens, totalOutputTokens, totalCost);
  }

  private List<DailyRollup> fetchRollups(Long projectId, LocalDate from, LocalDate to) {
    return projectId != null
        ? dailyRollupRepository.findByProjectIdAndDateBetween(projectId, from, to)
        : dailyRollupRepository.findByDateBetween(from, to);
  }
}
