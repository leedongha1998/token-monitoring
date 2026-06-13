package com.dongha.monitoring.pricing.service;

import com.dongha.monitoring.pricing.domain.ModelPricing;
import java.math.BigDecimal;
import java.time.Instant;

public record ModelPricingResult(
    Long id,
    String model,
    BigDecimal inputPricePerMToken,
    BigDecimal outputPricePerMToken,
    Instant effectiveFrom) {

  public static ModelPricingResult from(ModelPricing pricing) {
    return new ModelPricingResult(
        pricing.getId(),
        pricing.getModel(),
        pricing.getInputPricePerMToken(),
        pricing.getOutputPricePerMToken(),
        pricing.getEffectiveFrom());
  }
}
