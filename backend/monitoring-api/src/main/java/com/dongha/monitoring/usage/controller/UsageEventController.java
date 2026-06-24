package com.dongha.monitoring.usage.controller;

import com.dongha.monitoring.project.service.PageResult;
import com.dongha.monitoring.usage.service.BatchIngestRequest;
import com.dongha.monitoring.usage.service.BatchIngestResponse;
import com.dongha.monitoring.usage.service.IngestStatus;
import com.dongha.monitoring.usage.service.UsageEventRequest;
import com.dongha.monitoring.usage.service.UsageEventResult;
import com.dongha.monitoring.usage.service.UsageEventService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping
  public PageResult<UsageEventListResponse> list(
      @RequestParam(required = false) Long projectId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(required = false) String model,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    PageResult<UsageEventResult> raw =
        usageEventService.findEvents(projectId, from, to, model, page, size);
    List<UsageEventListResponse> content =
        raw.content().stream().map(UsageEventListResponse::from).toList();
    return new PageResult<>(content, raw.totalElements(), raw.totalPages(), raw.number());
  }
}
