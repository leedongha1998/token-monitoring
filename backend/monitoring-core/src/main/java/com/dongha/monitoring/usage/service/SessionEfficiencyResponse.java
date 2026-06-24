package com.dongha.monitoring.usage.service;

import java.math.BigDecimal;
import java.time.Instant;

public record SessionEfficiencyResponse(
    String sessionId,
    Instant sessionDate,
    String model,
    long totalInputTokens,
    long totalOutputTokens,
    BigDecimal costUsd) {}
