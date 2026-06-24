package com.dongha.monitoring.advisor.service;

import com.dongha.monitoring.pricing.service.ModelPricingService;
import com.dongha.monitoring.rollup.domain.DailyRollup;
import com.dongha.monitoring.rollup.repository.DailyRollupRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdvisorService {

  private final DailyRollupRepository dailyRollupRepository;
  private final ModelPricingService modelPricingService;

  public AdvisorService(
      DailyRollupRepository dailyRollupRepository, ModelPricingService modelPricingService) {
    this.dailyRollupRepository = dailyRollupRepository;
    this.modelPricingService = modelPricingService;
  }

  public List<ModelSwitchResponse> getModelSwitchAdvice(Long projectId) {
    LocalDate to = LocalDate.now();
    LocalDate from = to.minusDays(30);
    List<DailyRollup> rollups =
        dailyRollupRepository.findByProjectIdAndDateBetweenExcludingSyntheticModel(
            projectId, from, to);

    Map<String, long[]> tokensByModel = new HashMap<>();
    Map<String, BigDecimal> costByModel = new HashMap<>();
    for (DailyRollup r : rollups) {
      String m = r.getModel();
      tokensByModel.merge(
          m,
          new long[] {r.getTotalInputTokens(), r.getTotalOutputTokens()},
          (a, b) -> new long[] {a[0] + b[0], a[1] + b[1]});
      costByModel.merge(m, r.getTotalCost(), BigDecimal::add);
    }

    List<ModelSwitchResponse> results = new ArrayList<>();
    Instant now = Instant.now();
    for (Map.Entry<String, long[]> entry : tokensByModel.entrySet()) {
      String currentModel = entry.getKey();
      String suggestedModel = suggestAlternative(currentModel);
      if (suggestedModel == null) continue;
      modelPricingService
          .findEffectivePricing(suggestedModel, now)
          .ifPresent(
              pricing -> {
                long[] tokens = entry.getValue();
                BigDecimal projectedInput =
                    pricing
                        .inputPricePerMToken()
                        .multiply(BigDecimal.valueOf(tokens[0]))
                        .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);
                BigDecimal projectedOutput =
                    pricing
                        .outputPricePerMToken()
                        .multiply(BigDecimal.valueOf(tokens[1]))
                        .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP);
                BigDecimal projectedCost = projectedInput.add(projectedOutput);
                BigDecimal currentCost = costByModel.getOrDefault(currentModel, BigDecimal.ZERO);
                BigDecimal savings = currentCost.subtract(projectedCost);
                if (savings.compareTo(BigDecimal.ZERO) > 0) {
                  results.add(
                      new ModelSwitchResponse(
                          currentModel,
                          suggestedModel,
                          currentCost,
                          savings,
                          tokens[0],
                          tokens[1]));
                }
              });
    }
    return results;
  }

  private String suggestAlternative(String model) {
    if (model.contains("opus")) return model.replace("opus", "sonnet");
    if (model.contains("sonnet")) return model.replace("sonnet", "haiku");
    return null;
  }
}
