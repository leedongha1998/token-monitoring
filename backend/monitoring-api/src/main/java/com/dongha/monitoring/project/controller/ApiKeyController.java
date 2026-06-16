package com.dongha.monitoring.project.controller;

import com.dongha.monitoring.project.service.ApiKeyService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class ApiKeyController {

  private final ApiKeyService apiKeyService;

  public ApiKeyController(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @GetMapping("/v1/projects/{projectId}/api-keys")
  public List<ApiKeyListResponse> list(@PathVariable Long projectId) {
    return apiKeyService.listByProject(projectId).stream().map(ApiKeyListResponse::from).toList();
  }

  @PostMapping("/v1/projects/{projectId}/api-keys")
  public ResponseEntity<ApiKeyIssueResponse> issue(@PathVariable Long projectId) {
    ApiKeyIssueResponse body = ApiKeyIssueResponse.from(apiKeyService.issueKey(projectId));
    URI location =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/v1/api-keys/{id}")
            .buildAndExpand(body.id())
            .toUri();
    return ResponseEntity.created(location).body(body);
  }

  @DeleteMapping("/v1/api-keys/{id}")
  public ResponseEntity<Void> deactivate(@PathVariable Long id) {
    apiKeyService.deactivateKey(id);
    return ResponseEntity.noContent().build();
  }
}
