package com.dongha.monitoring.project.repository;

import com.dongha.monitoring.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {}
