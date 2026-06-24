package com.dongha.monitoring.project.service;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.domain.Project;
import com.dongha.monitoring.project.repository.ProjectRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Transactional
  public ProjectResult createProject(String name, String description) {
    if (name == null || name.isBlank() || name.length() > 100) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    return ProjectResult.from(projectRepository.save(Project.create(name, description)));
  }

  public ProjectResult findById(Long id) {
    return projectRepository
        .findById(id)
        .map(ProjectResult::from)
        .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
  }

  @Transactional
  public Long findOrCreateByDirectoryName(String dirName) {
    String name = dirName.length() > 100 ? dirName.substring(0, 100) : dirName;
    return projectRepository
        .findByName(name)
        .orElseGet(() -> projectRepository.save(Project.create(name, "Claude Code 자동 감지")))
        .getId();
  }

  public PageResult<ProjectResult> findAll(int page, int size) {
    var springPage =
        projectRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return new PageResult<>(
        springPage.getContent().stream().map(ProjectResult::from).toList(),
        springPage.getTotalElements(),
        springPage.getTotalPages(),
        springPage.getNumber());
  }
}
