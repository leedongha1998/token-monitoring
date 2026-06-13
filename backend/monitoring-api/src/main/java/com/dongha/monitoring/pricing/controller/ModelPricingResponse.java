package com.dongha.monitoring.pricing.controller;

import com.dongha.monitoring.pricing.service.ModelPricingResult;
import java.math.BigDecimal;

public record ModelPricingResponse(
    Long id,
    String model,
    BigDecimal inputPricePerMToken,
    BigDecimal outputPricePerMToken,
    String effectiveFrom) {

  public static ModelPricingResponse from(ModelPricingResult result) {
    return new ModelPricingResponse(
        result.id(),
        result.model(),
        result.inputPricePerMToken(),
        result.outputPricePerMToken(),
        result.effectiveFrom().toString());
  }
}
