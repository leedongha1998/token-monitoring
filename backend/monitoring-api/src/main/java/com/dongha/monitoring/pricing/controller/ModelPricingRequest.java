package com.dongha.monitoring.pricing.controller;

import java.math.BigDecimal;
import java.time.Instant;

public record ModelPricingRequest(
    String model,
    BigDecimal inputPricePerMToken,
    BigDecimal outputPricePerMToken,
    Instant effectiveFrom) {}
