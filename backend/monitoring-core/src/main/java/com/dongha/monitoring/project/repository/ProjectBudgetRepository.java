package com.dongha.monitoring.project.repository;

import com.dongha.monitoring.project.domain.ProjectBudget;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectBudgetRepository
    extends JpaRepository<ProjectBudget, Long>, ProjectBudgetQueryPort {

  Optional<ProjectBudget> findByProjectIdAndYearMonth(Long projectId, String yearMonth);
}
