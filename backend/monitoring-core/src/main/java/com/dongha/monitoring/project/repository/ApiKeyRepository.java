package com.dongha.monitoring.project.repository;

import com.dongha.monitoring.project.domain.ApiKey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

  Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);
}
