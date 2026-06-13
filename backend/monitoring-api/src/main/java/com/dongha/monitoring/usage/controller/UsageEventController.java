package com.dongha.monitoring.usage.controller;

import com.dongha.monitoring.usage.service.BatchIngestRequest;
import com.dongha.monitoring.usage.service.BatchIngestResponse;
import com.dongha.monitoring.usage.service.IngestStatus;
import com.dongha.monitoring.usage.service.UsageEventRequest;
import com.dongha.monitoring.usage.service.UsageEventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/events")
public class UsageEventController {

  private final UsageEventService usageEventService;

  public UsageEventController(UsageEventService usageEventService) {
    this.usageEventService = usageEventService;
  }

  @PostMapping
  public ResponseEntity<Void> ingest(
      HttpServletRequest httpRequest, @RequestBody UsageEventRequest request) {
    Long projectId = (Long) httpRequest.getAttribute("authenticatedProjectId");
    IngestStatus status = usageEventService.ingest(projectId, request);
    return status == IngestStatus.DUPLICATED
        ? ResponseEntity.ok().build()
        : ResponseEntity.accepted().build();
  }

  @PostMapping("/batch")
  public ResponseEntity<BatchIngestResponse> ingestBatch(
      HttpServletRequest httpRequest, @RequestBody BatchIngestRequest request) {
    Long projectId = (Long) httpRequest.getAttribute("authenticatedProjectId");
    BatchIngestResponse response = usageEventService.ingestBatch(projectId, request.events());
    return ResponseEntity.accepted().body(response);
  }
}
