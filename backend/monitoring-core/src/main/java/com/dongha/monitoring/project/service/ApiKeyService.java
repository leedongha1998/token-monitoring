package com.dongha.monitoring.project.service;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.domain.ApiKey;
import com.dongha.monitoring.project.domain.Project;
import com.dongha.monitoring.project.repository.ApiKeyRepository;
import com.dongha.monitoring.project.repository.ProjectRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApiKeyService {

  private final ApiKeyRepository apiKeyRepository;
  private final ProjectRepository projectRepository;

  public ApiKeyService(ApiKeyRepository apiKeyRepository, ProjectRepository projectRepository) {
    this.apiKeyRepository = apiKeyRepository;
    this.projectRepository = projectRepository;
  }

  public boolean validateKey(String rawKey) {
    return apiKeyRepository.findByKeyHashAndActiveTrue(ApiKey.hashKey(rawKey)).isPresent();
  }

  public Optional<Long> findProjectIdByKey(String rawKey) {
    if (rawKey == null || rawKey.isBlank()) return Optional.empty();
    return apiKeyRepository
        .findByKeyHashAndActiveTrue(ApiKey.hashKey(rawKey))
        .map(key -> key.getProject().getId());
  }

  @Transactional
  public ApiKeyResult issueKey(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    String rawKey = UUID.randomUUID().toString().replace("-", "");
    ApiKey apiKey = apiKeyRepository.save(ApiKey.create(project, rawKey));
    return new ApiKeyResult(apiKey.getId(), apiKey.getPrefix(), rawKey, apiKey.getCreatedAt());
  }

  @Transactional
  public void deactivateKey(Long keyId) {
    ApiKey apiKey =
        apiKeyRepository
            .findById(keyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.API_KEY_NOT_FOUND));
    apiKey.deactivate();
  }
}
