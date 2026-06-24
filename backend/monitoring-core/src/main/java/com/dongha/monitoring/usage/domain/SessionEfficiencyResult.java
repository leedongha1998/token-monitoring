package com.dongha.monitoring.usage.domain;

import java.time.Instant;

public record SessionEfficiencyResult(
    String sessionId,
    Long projectId,
    Instant sessionDate,
    String model,
    long totalInputTokens,
    long totalOutputTokens) {}
