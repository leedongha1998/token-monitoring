package com.dongha.monitoring.project.controller;

import com.dongha.monitoring.project.service.PageResult;
import com.dongha.monitoring.project.service.ProjectService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> create(@RequestBody ProjectRequest request) {
    var result = projectService.createProject(request.name(), request.description());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(result.id())
            .toUri();
    return ResponseEntity.created(location).body(ProjectResponse.from(result));
  }

  @GetMapping
  public PageResult<ProjectResponse> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    var pageResult = projectService.findAll(page, size);
    List<ProjectResponse> content =
        pageResult.content().stream().map(ProjectResponse::from).toList();
    return new PageResult<>(
        content, pageResult.totalElements(), pageResult.totalPages(), pageResult.number());
  }

  @GetMapping("/{id}")
  public ProjectResponse get(@PathVariable Long id) {
    return ProjectResponse.from(projectService.findById(id));
  }
}
