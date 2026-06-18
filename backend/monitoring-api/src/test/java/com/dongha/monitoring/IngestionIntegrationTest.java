package com.dongha.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongha.monitoring.rollup.service.DailyRollupService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class IngestionIntegrationTest extends AbstractIntegrationTest {

  @Autowired private DailyRollupService dailyRollupService;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  private String rawApiKey;

  @BeforeEach
  void setUp() throws Exception {
    HttpHeaders jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    // 프로젝트 생성
    String projectName = "통합테스트-" + UUID.randomUUID().toString().substring(0, 8);
    ResponseEntity<String> projectRes =
        restTemplate.exchange(
            "/v1/projects",
            HttpMethod.POST,
            new HttpEntity<>(
                "{\"name\":\"" + projectName + "\",\"description\":\"통합 테스트용\"}", jsonHeaders),
            String.class);
    assertThat(projectRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String location = projectRes.getHeaders().getLocation().toString();
    long projectId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

    // API 키 발급
    ResponseEntity<String> keyRes =
        restTemplate.exchange(
            "/v1/projects/" + projectId + "/api-keys",
            HttpMethod.POST,
            new HttpEntity<>(null, jsonHeaders),
            String.class);
    assertThat(keyRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode keyJson = objectMapper.readTree(keyRes.getBody());
    rawApiKey = keyJson.get("plainKey").asText();
  }

  @Test
  void 이벤트_수집_후_롤업하면_일별_통계에서_조회된다() {
    // given
    String body =
        "{\"idempotencyKey\":\"it-key-001\",\"model\":\"claude-sonnet-4-5\","
            + "\"inputTokens\":1000,\"outputTokens\":500,\"occurredAt\":\"2026-06-14T10:00:00Z\"}";

    // when: 이벤트 수집
    ResponseEntity<String> ingestResponse =
        restTemplate.exchange(
            "/v1/events", HttpMethod.POST, new HttpEntity<>(body, apiKeyHeaders()), String.class);

    // then: 202 Accepted
    assertThat(ingestResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    // when: 롤업 실행
    dailyRollupService.rollup(LocalDate.of(2026, 6, 14));

    // then: 일별 통계에 해당 모델이 포함됨
    ResponseEntity<String> statsResponse =
        restTemplate.getForEntity("/v1/stats/daily?from=2026-06-14&to=2026-06-14", String.class);
    assertThat(statsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(statsResponse.getBody()).contains("claude-sonnet-4-5");
  }

  @Test
  void 동일한_idempotencyKey로_재요청하면_이벤트가_중복_처리되지_않는다() {
    // given
    String body =
        "{\"idempotencyKey\":\"it-key-dup-001\",\"model\":\"claude-sonnet-4-5\","
            + "\"inputTokens\":100,\"outputTokens\":50,\"occurredAt\":\"2026-06-14T10:00:00Z\"}";
    HttpEntity<String> request = new HttpEntity<>(body, apiKeyHeaders());

    // when: 동일 키로 두 번 요청
    ResponseEntity<String> firstResponse =
        restTemplate.exchange("/v1/events", HttpMethod.POST, request, String.class);
    ResponseEntity<String> secondResponse =
        restTemplate.exchange("/v1/events", HttpMethod.POST, request, String.class);

    // then: 첫 번째 202(신규 수집), 두 번째 200(멱등 처리)
    assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private HttpHeaders apiKeyHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", rawApiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
