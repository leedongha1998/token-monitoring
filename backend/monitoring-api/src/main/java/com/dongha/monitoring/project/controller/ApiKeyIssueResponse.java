package com.dongha.monitoring.project.controller;

import com.dongha.monitoring.project.service.ApiKeyResult;
import java.time.Instant;

public record ApiKeyIssueResponse(Long id, String prefix, String plainKey, Instant createdAt) {

  public static ApiKeyIssueResponse from(ApiKeyResult result) {
    return new ApiKeyIssueResponse(
        result.id(), result.prefix(), result.rawKey(), result.createdAt());
  }
}
