package com.dongha.monitoring.usage.repository;

import com.dongha.monitoring.usage.domain.SessionEfficiencyResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UsageEventQueryPort {

  List<SessionEfficiencyResult> findSessionEfficiency(Long projectId, Instant from, Instant to);

  Optional<Double> findAverageInputTokensByProjectAndDateRange(
      Long projectId, Instant from, Instant to);

  List<Long> findEventIdsWithHighInputTokens(
      Long projectId, Instant from, Instant to, long threshold);
}
