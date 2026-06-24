package com.dongha.monitoring.usage.repository;

import com.dongha.monitoring.usage.domain.SessionEfficiencyResult;
import com.dongha.monitoring.usage.domain.UsageAggregateResult;
import com.dongha.monitoring.usage.domain.UsageEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsageEventRepository extends JpaRepository<UsageEvent, Long>, UsageEventQueryPort {

  Optional<UsageEvent> findByIdempotencyKey(String idempotencyKey);

  List<UsageEvent> findByRawPayloadIsNotNull();

  @Query("SELECT MIN(u.occurredAt) FROM UsageEvent u")
  Optional<Instant> findEarliestOccurredAt();

  boolean existsByIdempotencyKey(String idempotencyKey);

  @Query(
      "SELECT u FROM UsageEvent u WHERE u.projectId = :projectId"
          + " AND u.occurredAt >= :from AND u.occurredAt < :to")
  Page<UsageEvent> findByProjectAndDateRange(
      @Param("projectId") Long projectId,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable pageable);

  @Query("SELECT u FROM UsageEvent u WHERE u.occurredAt >= :from AND u.occurredAt < :to")
  Page<UsageEvent> findByDateRange(
      @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

  @Query(
      "SELECT u FROM UsageEvent u WHERE u.projectId = :projectId"
          + " AND u.occurredAt >= :from AND u.occurredAt < :to"
          + " AND u.model = :model")
  Page<UsageEvent> findByProjectAndDateRangeAndModel(
      @Param("projectId") Long projectId,
      @Param("from") Instant from,
      @Param("to") Instant to,
      @Param("model") String model,
      Pageable pageable);

  @Query(
      "SELECT u FROM UsageEvent u WHERE u.occurredAt >= :from AND u.occurredAt < :to"
          + " AND u.model = :model")
  Page<UsageEvent> findByDateRangeAndModel(
      @Param("from") Instant from,
      @Param("to") Instant to,
      @Param("model") String model,
      Pageable pageable);

  @Query(
      "SELECT new com.dongha.monitoring.usage.domain.UsageAggregateResult("
          + "u.projectId, u.model, SUM(u.inputTokens), SUM(u.outputTokens)) "
          + "FROM UsageEvent u "
          + "WHERE u.occurredAt >= :from AND u.occurredAt < :to "
          + "GROUP BY u.projectId, u.model")
  List<UsageAggregateResult> aggregateByDateRange(
      @Param("from") Instant from, @Param("to") Instant to);

  @Query(
      "SELECT new com.dongha.monitoring.usage.domain.SessionEfficiencyResult("
          + "u.sessionId, u.projectId, MIN(u.occurredAt), u.model, "
          + "SUM(u.inputTokens), SUM(u.outputTokens)) "
          + "FROM UsageEvent u "
          + "WHERE u.projectId = :projectId "
          + "AND u.occurredAt >= :from AND u.occurredAt < :to "
          + "AND u.sessionId IS NOT NULL "
          + "GROUP BY u.sessionId, u.projectId, u.model "
          + "ORDER BY MIN(u.occurredAt) DESC")
  List<SessionEfficiencyResult> findSessionEfficiency(
      @Param("projectId") Long projectId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(
      "SELECT AVG(u.inputTokens) FROM UsageEvent u "
          + "WHERE u.projectId = :projectId "
          + "AND u.occurredAt >= :from AND u.occurredAt < :to")
  Optional<Double> findAverageInputTokensByProjectAndDateRange(
      @Param("projectId") Long projectId, @Param("from") Instant from, @Param("to") Instant to);

  @Query(
      "SELECT u.id FROM UsageEvent u "
          + "WHERE u.projectId = :projectId "
          + "AND u.occurredAt >= :from AND u.occurredAt < :to "
          + "AND u.inputTokens > :threshold")
  List<Long> findEventIdsWithHighInputTokens(
      @Param("projectId") Long projectId,
      @Param("from") Instant from,
      @Param("to") Instant to,
      @Param("threshold") long threshold);
}
