package com.dongha.monitoring.usage.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.service.ApiKeyService;
import com.dongha.monitoring.project.service.PageResult;
import com.dongha.monitoring.usage.service.BatchIngestResponse;
import com.dongha.monitoring.usage.service.IngestResult;
import com.dongha.monitoring.usage.service.IngestStatus;
import com.dongha.monitoring.usage.service.UsageEventResult;
import com.dongha.monitoring.usage.service.UsageEventService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsageEventController.class)
class UsageEventControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean ApiKeyService apiKeyService;
  @MockBean UsageEventService usageEventService;

  private static final String VALID_KEY = "valid-api-key";
  private static final String SINGLE_EVENT_BODY =
      "{\"idempotencyKey\":\"idem-1\",\"model\":\"claude-sonnet-4-5\","
          + "\"inputTokens\":100,\"outputTokens\":50,\"occurredAt\":\"2026-06-13T00:00:00Z\"}";

  @Test
  void 신규_이벤트_수집시_202를_반환한다() throws Exception {
    // given
    when(apiKeyService.findProjectIdByKey(VALID_KEY)).thenReturn(Optional.of(1L));
    when(usageEventService.ingest(anyLong(), any())).thenReturn(IngestStatus.ACCEPTED);

    // when & then
    mockMvc
        .perform(
            post("/v1/events")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(SINGLE_EVENT_BODY))
        .andExpect(status().isAccepted());
  }

  @Test
  void 중복_idempotencyKey_재요청시_200을_반환한다() throws Exception {
    // given
    when(apiKeyService.findProjectIdByKey(VALID_KEY)).thenReturn(Optional.of(1L));
    when(usageEventService.ingest(anyLong(), any())).thenReturn(IngestStatus.DUPLICATED);

    // when & then
    mockMvc
        .perform(
            post("/v1/events")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(SINGLE_EVENT_BODY))
        .andExpect(status().isOk());
  }

  @Test
  void X_API_Key_헤더_없이_요청하면_401을_반환한다() throws Exception {
    mockMvc
        .perform(
            post("/v1/events").contentType(MediaType.APPLICATION_JSON).content(SINGLE_EVENT_BODY))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void 배치_수집_요청시_202와_건별_결과를_반환한다() throws Exception {
    // given
    when(apiKeyService.findProjectIdByKey(VALID_KEY)).thenReturn(Optional.of(1L));
    BatchIngestResponse batchResponse =
        new BatchIngestResponse(1, 0, List.of(new IngestResult("idem-1", IngestStatus.ACCEPTED)));
    when(usageEventService.ingestBatch(anyLong(), any())).thenReturn(batchResponse);

    String batchBody =
        "{\"events\":["
            + "{\"idempotencyKey\":\"idem-1\",\"model\":\"claude-sonnet-4-5\","
            + "\"inputTokens\":100,\"outputTokens\":50,\"occurredAt\":\"2026-06-13T00:00:00Z\"}"
            + "]}";

    // when & then
    mockMvc
        .perform(
            post("/v1/events/batch")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchBody))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.accepted").value(1))
        .andExpect(jsonPath("$.duplicated").value(0))
        .andExpect(jsonPath("$.results[0].idempotencyKey").value("idem-1"))
        .andExpect(jsonPath("$.results[0].status").value("ACCEPTED"));
  }

  @Test
  void 배치_이벤트가_100건_초과하면_413을_반환한다() throws Exception {
    // given
    when(apiKeyService.findProjectIdByKey(VALID_KEY)).thenReturn(Optional.of(1L));
    when(usageEventService.ingestBatch(anyLong(), any()))
        .thenThrow(new BusinessException(ErrorCode.BATCH_SIZE_EXCEEDED));

    String batchBody =
        "{\"events\":["
            + "{\"idempotencyKey\":\"idem-1\",\"model\":\"claude-sonnet-4-5\","
            + "\"inputTokens\":1,\"outputTokens\":1,\"occurredAt\":\"2026-06-13T00:00:00Z\"}"
            + "]}";

    // when & then
    mockMvc
        .perform(
            post("/v1/events/batch")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(batchBody))
        .andExpect(status().isPayloadTooLarge())
        .andExpect(jsonPath("$.code").value("USAGE-001"));
  }

  @Test
  void 이벤트_목록_조회시_200과_페이지_결과를_반환한다() throws Exception {
    // given
    Instant occurredAt = Instant.parse("2026-06-13T00:00:00Z");
    UsageEventResult item =
        new UsageEventResult(1L, "claude-sonnet-4-5", 100, 50, occurredAt, null, null);
    PageResult<UsageEventResult> pageResult = new PageResult<>(List.of(item), 1L, 1, 0);
    when(usageEventService.findEvents(
            isNull(), any(Instant.class), any(Instant.class), isNull(), anyInt(), anyInt()))
        .thenReturn(pageResult);

    // when & then
    mockMvc
        .perform(
            get("/v1/events")
                .param("from", "2026-06-13T00:00:00Z")
                .param("to", "2026-06-14T00:00:00Z"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].model").value("claude-sonnet-4-5"));
  }
}
