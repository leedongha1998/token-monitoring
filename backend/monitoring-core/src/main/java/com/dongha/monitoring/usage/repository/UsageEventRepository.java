package com.dongha.monitoring.usage.repository;

import com.dongha.monitoring.usage.domain.UsageEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {

  Optional<UsageEvent> findByIdempotencyKey(String idempotencyKey);

  boolean existsByIdempotencyKey(String idempotencyKey);
}
