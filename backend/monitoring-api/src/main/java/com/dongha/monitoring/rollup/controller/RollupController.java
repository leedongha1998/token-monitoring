package com.dongha.monitoring.rollup.controller;

import com.dongha.monitoring.rollup.service.DailyRollupService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/rollup")
public class RollupController {

  private final DailyRollupService dailyRollupService;

  public RollupController(DailyRollupService dailyRollupService) {
    this.dailyRollupService = dailyRollupService;
  }

  @PostMapping("/trigger")
  public ResponseEntity<Void> trigger(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    dailyRollupService.rollup(date);
    return ResponseEntity.accepted().build();
  }
}
