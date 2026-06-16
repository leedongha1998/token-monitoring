package com.dongha.monitoring.jsonl;

import java.time.Instant;

public record JsonlEntry(
    String idempotencyKey,
    String model,
    int inputTokens,
    int outputTokens,
    Instant occurredAt,
    String promptSummary) {}
