package com.dongha.monitoring.advisor.service;

import java.math.BigDecimal;

public record ModelSwitchResponse(
    String currentModel,
    String suggestedModel,
    BigDecimal currentMonthlyCost,
    BigDecimal projectedSavings,
    long monthlyInputTokens,
    long monthlyOutputTokens) {}
