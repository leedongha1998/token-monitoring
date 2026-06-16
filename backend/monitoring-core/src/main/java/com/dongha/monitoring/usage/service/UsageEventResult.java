package com.dongha.monitoring.usage.service;

import com.dongha.monitoring.usage.domain.UsageEvent;
import java.time.Instant;

public record UsageEventResult(
    Long id,
    String model,
    int inputTokens,
    int outputTokens,
    Instant occurredAt,
    String promptSummary) {

  public static UsageEventResult from(UsageEvent event) {
    return new UsageEventResult(
        event.getId(),
        event.getModel(),
        event.getInputTokens(),
        event.getOutputTokens(),
        event.getOccurredAt(),
        event.getPromptSummary());
  }
}
