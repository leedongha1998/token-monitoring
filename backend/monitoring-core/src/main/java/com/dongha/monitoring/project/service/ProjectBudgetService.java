package com.dongha.monitoring.project.service;

import com.dongha.monitoring.project.domain.ProjectBudget;
import com.dongha.monitoring.project.repository.ProjectBudgetRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectBudgetService {

  private final ProjectBudgetRepository projectBudgetRepository;

  public ProjectBudgetService(ProjectBudgetRepository projectBudgetRepository) {
    this.projectBudgetRepository = projectBudgetRepository;
  }

  @Transactional
  public ProjectBudgetResult setBudget(
      Long projectId, String yearMonth, BigDecimal monthlyBudgetUsd) {
    ProjectBudget budget =
        projectBudgetRepository
            .findByProjectIdAndYearMonth(projectId, yearMonth)
            .map(
                existing -> {
                  existing.updateBudget(monthlyBudgetUsd);
                  return existing;
                })
            .orElseGet(
                () ->
                    projectBudgetRepository.save(
                        ProjectBudget.create(projectId, yearMonth, monthlyBudgetUsd)));
    return ProjectBudgetResult.from(budget);
  }

  public Optional<ProjectBudgetResult> getBudget(Long projectId, String yearMonth) {
    return projectBudgetRepository
        .findByProjectIdAndYearMonth(projectId, yearMonth)
        .map(ProjectBudgetResult::from);
  }
}
