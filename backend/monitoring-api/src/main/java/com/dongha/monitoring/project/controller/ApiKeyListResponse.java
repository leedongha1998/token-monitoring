package com.dongha.monitoring.project.controller;

import com.dongha.monitoring.project.service.ApiKeyListItem;
import java.time.Instant;

public record ApiKeyListResponse(Long id, String prefix, boolean active, Instant createdAt) {

  public static ApiKeyListResponse from(ApiKeyListItem item) {
    return new ApiKeyListResponse(item.id(), item.prefix(), item.active(), item.createdAt());
  }
}
