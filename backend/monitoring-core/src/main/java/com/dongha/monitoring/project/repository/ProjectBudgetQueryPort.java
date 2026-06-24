package com.dongha.monitoring.project.repository;

import com.dongha.monitoring.project.domain.ProjectBudget;
import java.util.Optional;

public interface ProjectBudgetQueryPort {

  Optional<ProjectBudget> findByProjectIdAndYearMonth(Long projectId, String yearMonth);
}
