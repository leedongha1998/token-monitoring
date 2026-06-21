package com.dongha.monitoring.rollup.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongha.monitoring.AbstractIntegrationTest;
import com.dongha.monitoring.rollup.service.DailyRollupService;
import com.dongha.monitoring.rollup.service.DailyStatsResponse;
import com.dongha.monitoring.rollup.service.StatsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class DailyRollupRepositoryTest extends AbstractIntegrationTest {

  @Autowired private DailyRollupService dailyRollupService;
  @Autowired private StatsService statsService;
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  private Long projectId;
  private String rawApiKey;

  private static final LocalDate TEST_DATE = LocalDate.of(2026, 6, 21);

  @BeforeEach
  void setUp() throws Exception {
    HttpHeaders jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

    String projectName = "rollup-repo-test-" + UUID.randomUUID().toString().substring(0, 8);
    ResponseEntity<String> projectRes =
        restTemplate.exchange(
            "/v1/projects",
            HttpMethod.POST,
            new HttpEntity<>(
                "{\"name\":\"" + projectName + "\",\"description\":\"레포지터리 테스트용\"}", jsonHeaders),
            String.class);
    String location = projectRes.getHeaders().getLocation().toString();
    projectId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

    ResponseEntity<String> keyRes =
        restTemplate.exchange(
            "/v1/projects/" + projectId + "/api-keys",
            HttpMethod.POST,
            new HttpEntity<>(null, jsonHeaders),
            String.class);
    JsonNode keyJson = objectMapper.readTree(keyRes.getBody());
    rawApiKey = keyJson.get("plainKey").asText();
  }

  @Test
  void synthetic_모델_항목은_일별_통계_조회에서_제외된다() throws Exception {
    // given — 실제 모델과 <synthetic> 모델 이벤트를 각각 수집한 뒤 롤업 실행
    ingest("real-" + UUID.randomUUID(), "claude-sonnet-4-5", TEST_DATE);
    ingest("synth-" + UUID.randomUUID(), "<synthetic>", TEST_DATE);
    dailyRollupService.rollup(TEST_DATE);

    // when
    List<DailyStatsResponse> result =
        statsService.getDailyStats(projectId, TEST_DATE, TEST_DATE, null);

    // then — <synthetic> 항목이 제외되고 실제 모델만 포함된다
    assertThat(result)
        .extracting(DailyStatsResponse::model)
        .containsExactly("claude-sonnet-4-5")
        .doesNotContain("<synthetic>");
  }

  private void ingest(String idempotencyKey, String model, LocalDate date) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", rawApiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body =
        String.format(
            "{\"idempotencyKey\":\"%s\",\"model\":\"%s\","
                + "\"inputTokens\":100,\"outputTokens\":50,\"occurredAt\":\"%sT10:00:00Z\"}",
            idempotencyKey, model, date);
    restTemplate.exchange(
        "/v1/events", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
  }
}
