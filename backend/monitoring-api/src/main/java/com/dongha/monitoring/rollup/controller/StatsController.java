package com.dongha.monitoring.rollup.controller;

import com.dongha.monitoring.rollup.service.DailyStatsResponse;
import com.dongha.monitoring.rollup.service.StatsService;
import com.dongha.monitoring.rollup.service.SummaryStatsResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stats")
public class StatsController {

  private final StatsService statsService;

  public StatsController(StatsService statsService) {
    this.statsService = statsService;
  }

  @GetMapping("/daily")
  public ResponseEntity<List<DailyStatsResponse>> daily(
      @RequestParam(required = false) Long projectId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) String model) {
    return ResponseEntity.ok(statsService.getDailyStats(projectId, from, to, model));
  }

  @GetMapping("/summary")
  public ResponseEntity<SummaryStatsResponse> summary(
      @RequestParam(required = false) Long projectId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(statsService.getSummary(projectId, from, to));
  }
}
