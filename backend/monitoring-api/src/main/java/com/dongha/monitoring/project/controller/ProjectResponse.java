package com.dongha.monitoring.project.controller;

import com.dongha.monitoring.project.service.ProjectResult;
import java.time.Instant;

public record ProjectResponse(
    Long id, String name, String description, boolean active, Instant createdAt) {

  public static ProjectResponse from(ProjectResult result) {
    return new ProjectResponse(
        result.id(), result.name(), result.description(), result.active(), result.createdAt());
  }
}
