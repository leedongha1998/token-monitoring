package com.dongha.monitoring.usage.service;

import com.dongha.monitoring.pricing.service.ModelPricingResult;
import com.dongha.monitoring.usage.domain.UsageEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

public record UsageEventResult(
    Long id,
    String model,
    int inputTokens,
    int outputTokens,
    Instant occurredAt,
    String promptSummary,
    BigDecimal cost) {

  private static final BigDecimal M_TOKENS = new BigDecimal("1000000");

  public static UsageEventResult from(UsageEvent event) {
    return new UsageEventResult(
        event.getId(),
        event.getModel(),
        event.getInputTokens(),
        event.getOutputTokens(),
        event.getOccurredAt(),
        event.getPromptSummary(),
        null);
  }

  public static UsageEventResult from(UsageEvent event, Optional<ModelPricingResult> pricing) {
    BigDecimal cost =
        pricing
            .map(
                p ->
                    p.inputPricePerMToken()
                        .multiply(BigDecimal.valueOf(event.getInputTokens()))
                        .divide(M_TOKENS, 8, RoundingMode.HALF_UP)
                        .add(
                            p.outputPricePerMToken()
                                .multiply(BigDecimal.valueOf(event.getOutputTokens()))
                                .divide(M_TOKENS, 8, RoundingMode.HALF_UP)))
            .orElse(null);
    return new UsageEventResult(
        event.getId(),
        event.getModel(),
        event.getInputTokens(),
        event.getOutputTokens(),
        event.getOccurredAt(),
        event.getPromptSummary(),
        cost);
  }
}
