package com.dongha.monitoring.project.service;

import com.dongha.monitoring.project.domain.Project;
import java.time.Instant;

public record ProjectResult(
    Long id, String name, String description, boolean active, Instant createdAt) {

  public static ProjectResult from(Project project) {
    return new ProjectResult(
        project.getId(),
        project.getName(),
        project.getDescription(),
        project.isActive(),
        project.getCreatedAt());
  }
}
