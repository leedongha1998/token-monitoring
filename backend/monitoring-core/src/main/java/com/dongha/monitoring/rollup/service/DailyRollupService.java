package com.dongha.monitoring.rollup.service;

import com.dongha.monitoring.pricing.repository.ModelPricingRepository;
import com.dongha.monitoring.rollup.domain.DailyRollup;
import com.dongha.monitoring.rollup.repository.DailyRollupRepository;
import com.dongha.monitoring.usage.domain.UsageAggregateResult;
import com.dongha.monitoring.usage.repository.UsageEventRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyRollupService {

  private static final BigDecimal M_TOKEN_DIVISOR = new BigDecimal("1000000");

  private final UsageEventRepository usageEventRepository;
  private final ModelPricingRepository modelPricingRepository;
  private final DailyRollupRepository dailyRollupRepository;

  public DailyRollupService(
      UsageEventRepository usageEventRepository,
      ModelPricingRepository modelPricingRepository,
      DailyRollupRepository dailyRollupRepository) {
    this.usageEventRepository = usageEventRepository;
    this.modelPricingRepository = modelPricingRepository;
    this.dailyRollupRepository = dailyRollupRepository;
  }

  @Transactional
  public void rollup(LocalDate date) {
    Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

    dailyRollupRepository.deleteByDate(date);

    List<UsageAggregateResult> aggregates = usageEventRepository.aggregateByDateRange(from, to);
    for (UsageAggregateResult agg : aggregates) {
      BigDecimal totalCost = calculateCost(agg, from);
      dailyRollupRepository.save(
          DailyRollup.create(
              agg.projectId(),
              date,
              agg.model(),
              agg.totalInputTokens(),
              agg.totalOutputTokens(),
              totalCost));
    }
  }

  @Transactional
  public void rollupMissingDates(LocalDate today) {
    Instant earliest = usageEventRepository.findEarliestOccurredAt().orElse(null);
    if (earliest == null) return;
    LocalDate startDate = earliest.atZone(ZoneOffset.UTC).toLocalDate();
    Set<LocalDate> alreadyRolledUp = new HashSet<>(dailyRollupRepository.findDistinctDates());
    for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
      if (!alreadyRolledUp.contains(date)) {
        rollup(date);
      }
    }
  }

  private BigDecimal calculateCost(UsageAggregateResult agg, Instant at) {
    return modelPricingRepository
        .findTopByModelAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(agg.model(), at)
        .map(
            pricing -> {
              BigDecimal inputCost =
                  pricing
                      .getInputPricePerMToken()
                      .multiply(new BigDecimal(agg.totalInputTokens()))
                      .divide(M_TOKEN_DIVISOR, 8, RoundingMode.HALF_UP);
              BigDecimal outputCost =
                  pricing
                      .getOutputPricePerMToken()
                      .multiply(new BigDecimal(agg.totalOutputTokens()))
                      .divide(M_TOKEN_DIVISOR, 8, RoundingMode.HALF_UP);
              return inputCost.add(outputCost);
            })
        .orElse(BigDecimal.ZERO);
  }
}
