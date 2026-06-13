package com.dongha.monitoring.usage.service;

import java.time.Instant;

public record UsageEventRequest(
    String idempotencyKey, String model, int inputTokens, int outputTokens, Instant occurredAt) {}
