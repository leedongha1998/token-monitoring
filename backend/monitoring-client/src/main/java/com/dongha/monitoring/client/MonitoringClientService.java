package com.dongha.monitoring.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringClientService {

  private static final Logger log = LoggerFactory.getLogger(MonitoringClientService.class);

  private final MonitoringClientProperties properties;
  private final HttpClient httpClient;

  public MonitoringClientService(MonitoringClientProperties properties) {
    this(
        properties,
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
            .build());
  }

  MonitoringClientService(MonitoringClientProperties properties, HttpClient httpClient) {
    this.properties = properties;
    this.httpClient = httpClient;
  }

  public void sendEvent(String model, int inputTokens, int outputTokens, String idempotencyKey) {
    if (!properties.enabled()) {
      return;
    }
    String body = buildJsonBody(model, inputTokens, outputTokens, idempotencyKey);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(properties.serverUrl() + "/v1/events"))
            .header("Content-Type", "application/json")
            .header("X-API-Key", properties.apiKey())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofMillis(properties.readTimeoutMs()))
            .build();
    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 202) {
        log.warn("모니터링 이벤트 전송 실패: status={}, body={}", response.statusCode(), response.body());
      }
    } catch (Exception e) {
      log.warn("모니터링 이벤트 전송 오류: {}", e.getMessage());
    }
  }

  private String buildJsonBody(
      String model, int inputTokens, int outputTokens, String idempotencyKey) {
    return String.format(
        "{\"idempotencyKey\":\"%s\",\"model\":\"%s\",\"inputTokens\":%d,\"outputTokens\":%d,\"occurredAt\":\"%s\"}",
        escapeJson(idempotencyKey), escapeJson(model), inputTokens, outputTokens, Instant.now());
  }

  private String escapeJson(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
