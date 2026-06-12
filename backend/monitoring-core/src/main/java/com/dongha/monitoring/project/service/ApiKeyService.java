package com.dongha.monitoring.project.service;

import com.dongha.monitoring.project.domain.ApiKey;
import com.dongha.monitoring.project.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApiKeyService {

  private final ApiKeyRepository apiKeyRepository;

  public ApiKeyService(ApiKeyRepository apiKeyRepository) {
    this.apiKeyRepository = apiKeyRepository;
  }

  public boolean validateKey(String rawKey) {
    return apiKeyRepository.findByKeyHashAndActiveTrue(ApiKey.hashKey(rawKey)).isPresent();
  }
}
