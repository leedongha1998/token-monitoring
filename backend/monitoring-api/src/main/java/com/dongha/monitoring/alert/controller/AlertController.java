package com.dongha.monitoring.alert.controller;

import com.dongha.monitoring.alert.service.AlertResult;
import com.dongha.monitoring.alert.service.AlertService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/alerts")
public class AlertController {

  private final AlertService alertService;

  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }

  @GetMapping
  public ResponseEntity<List<AlertResult>> list(@RequestParam Long projectId) {
    return ResponseEntity.ok(alertService.findByProject(projectId));
  }

  @PostMapping("/{id}/read")
  public ResponseEntity<Void> markRead(@PathVariable Long id) {
    alertService.markAsRead(id);
    return ResponseEntity.noContent().build();
  }
}
