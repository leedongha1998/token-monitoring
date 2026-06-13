package com.dongha.monitoring.usage.repository;

import com.dongha.monitoring.usage.domain.UsageAggregateResult;
import com.dongha.monitoring.usage.domain.UsageEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {

  Optional<UsageEvent> findByIdempotencyKey(String idempotencyKey);

  boolean existsByIdempotencyKey(String idempotencyKey);

  @Query(
      "SELECT new com.dongha.monitoring.usage.domain.UsageAggregateResult("
          + "u.projectId, u.model, SUM(u.inputTokens), SUM(u.outputTokens)) "
          + "FROM UsageEvent u "
          + "WHERE u.occurredAt >= :from AND u.occurredAt < :to "
          + "GROUP BY u.projectId, u.model")
  List<UsageAggregateResult> aggregateByDateRange(
      @Param("from") Instant from, @Param("to") Instant to);
}
