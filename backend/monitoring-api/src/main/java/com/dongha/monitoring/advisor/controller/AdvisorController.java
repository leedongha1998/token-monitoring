package com.dongha.monitoring.advisor.controller;

import com.dongha.monitoring.advisor.service.AdvisorService;
import com.dongha.monitoring.advisor.service.ModelSwitchResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/advisor")
public class AdvisorController {

  private final AdvisorService advisorService;

  public AdvisorController(AdvisorService advisorService) {
    this.advisorService = advisorService;
  }

  @GetMapping("/model-switch")
  public ResponseEntity<List<ModelSwitchResponse>> modelSwitch(@RequestParam Long projectId) {
    return ResponseEntity.ok(advisorService.getModelSwitchAdvice(projectId));
  }
}
