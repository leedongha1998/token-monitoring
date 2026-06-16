package com.dongha.monitoring.jsonl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JsonlScanScheduler {

  private final JsonlIngestService jsonlIngestService;

  public JsonlScanScheduler(JsonlIngestService jsonlIngestService) {
    this.jsonlIngestService = jsonlIngestService;
  }

  @Scheduled(fixedDelayString = "${jsonl.scan.interval:PT5M}")
  public void scan() {
    jsonlIngestService.scan();
  }
}
