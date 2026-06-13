package com.dongha.monitoring.usage.domain;

public record UsageAggregateResult(
    Long projectId, String model, Long totalInputTokens, Long totalOutputTokens) {}
