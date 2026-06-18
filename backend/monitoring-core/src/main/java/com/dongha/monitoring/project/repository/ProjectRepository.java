package com.dongha.monitoring.project.repository;

import com.dongha.monitoring.project.domain.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  Optional<Project> findByName(String name);
}
