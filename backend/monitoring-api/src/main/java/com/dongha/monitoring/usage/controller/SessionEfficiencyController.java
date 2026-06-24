package com.dongha.monitoring.usage.controller;

import com.dongha.monitoring.usage.service.SessionEfficiencyResponse;
import com.dongha.monitoring.usage.service.UsageEventService;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stats")
public class SessionEfficiencyController {

  private final UsageEventService usageEventService;

  public SessionEfficiencyController(UsageEventService usageEventService) {
    this.usageEventService = usageEventService;
  }

  @GetMapping("/session-efficiency")
  public ResponseEntity<List<SessionEfficiencyResponse>> sessionEfficiency(
      @RequestParam Long projectId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(
        usageEventService.getSessionEfficiency(
            projectId,
            from.atStartOfDay(ZoneOffset.UTC).toInstant(),
            to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
  }
}
