package com.dongha.monitoring.usage.controller;

import com.dongha.monitoring.usage.service.UsageEventResult;
import java.time.Instant;

public record UsageEventListResponse(
    Long id,
    String model,
    int inputTokens,
    int outputTokens,
    Instant occurredAt,
    String promptSummary) {

  public static UsageEventListResponse from(UsageEventResult result) {
    return new UsageEventListResponse(
        result.id(),
        result.model(),
        result.inputTokens(),
        result.outputTokens(),
        result.occurredAt(),
        result.promptSummary());
  }
}
