package com.dongha.monitoring.project.controller;

import com.dongha.monitoring.project.service.ProjectBudgetService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/projects")
public class ProjectBudgetController {

  private final ProjectBudgetService projectBudgetService;

  public ProjectBudgetController(ProjectBudgetService projectBudgetService) {
    this.projectBudgetService = projectBudgetService;
  }

  @PostMapping("/{id}/budget")
  public ResponseEntity<ProjectBudgetResponse> setBudget(
      @PathVariable Long id, @Valid @RequestBody ProjectBudgetRequest request) {
    ProjectBudgetResponse response =
        ProjectBudgetResponse.from(
            projectBudgetService.setBudget(id, request.yearMonth(), request.monthlyBudgetUsd()));
    return ResponseEntity.created(URI.create("/v1/projects/" + id + "/budget")).body(response);
  }

  @GetMapping("/{id}/budget")
  public ResponseEntity<ProjectBudgetResponse> getBudget(
      @PathVariable Long id, @RequestParam String yearMonth) {
    return projectBudgetService
        .getBudget(id, yearMonth)
        .map(result -> ResponseEntity.ok(ProjectBudgetResponse.from(result)))
        .orElse(ResponseEntity.notFound().build());
  }
}
