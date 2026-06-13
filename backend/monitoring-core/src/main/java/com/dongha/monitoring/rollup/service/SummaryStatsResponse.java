package com.dongha.monitoring.rollup.service;

import java.math.BigDecimal;

public record SummaryStatsResponse(
    long totalInputTokens, long totalOutputTokens, BigDecimal totalCost) {}
