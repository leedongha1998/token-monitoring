package com.dongha.monitoring.rollup.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongha.monitoring.project.service.ApiKeyService;
import com.dongha.monitoring.rollup.service.DailyStatsResponse;
import com.dongha.monitoring.rollup.service.StatsService;
import com.dongha.monitoring.rollup.service.SummaryStatsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean StatsService statsService;
  @MockBean ApiKeyService apiKeyService;

  @Test
  void 일별_통계_조회시_200과_결과_목록을_반환한다() throws Exception {
    // given
    DailyStatsResponse response =
        new DailyStatsResponse(
            1L,
            LocalDate.of(2026, 6, 12),
            "claude-sonnet-4-5",
            1000L,
            500L,
            new BigDecimal("0.01050000"));
    when(statsService.getDailyStats(eq(1L), any(), any(), isNull())).thenReturn(List.of(response));

    // when & then
    mockMvc
        .perform(
            get("/v1/stats/daily")
                .param("projectId", "1")
                .param("from", "2026-06-01")
                .param("to", "2026-06-30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].projectId").value(1))
        .andExpect(jsonPath("$[0].model").value("claude-sonnet-4-5"))
        .andExpect(jsonPath("$[0].totalInputTokens").value(1000))
        .andExpect(jsonPath("$[0].totalOutputTokens").value(500));
  }

  @Test
  void projectId_없이_일별_통계_조회시_전체_결과를_반환한다() throws Exception {
    // given
    when(statsService.getDailyStats(isNull(), any(), any(), isNull())).thenReturn(List.of());

    // when & then
    mockMvc
        .perform(get("/v1/stats/daily").param("from", "2026-06-01").param("to", "2026-06-30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void 요약_통계_조회시_200과_합계를_반환한다() throws Exception {
    // given
    SummaryStatsResponse response =
        new SummaryStatsResponse(10000L, 5000L, new BigDecimal("0.10500000"));
    when(statsService.getSummary(isNull(), any(), any())).thenReturn(response);

    // when & then
    mockMvc
        .perform(get("/v1/stats/summary").param("from", "2026-06-01").param("to", "2026-06-30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalInputTokens").value(10000))
        .andExpect(jsonPath("$.totalOutputTokens").value(5000));
  }

  @Test
  void projectId로_요약_통계_조회시_필터링된_합계를_반환한다() throws Exception {
    // given
    SummaryStatsResponse response =
        new SummaryStatsResponse(3000L, 1500L, new BigDecimal("0.03150000"));
    when(statsService.getSummary(eq(1L), any(), any())).thenReturn(response);

    // when & then
    mockMvc
        .perform(
            get("/v1/stats/summary")
                .param("projectId", "1")
                .param("from", "2026-06-01")
                .param("to", "2026-06-30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalInputTokens").value(3000))
        .andExpect(jsonPath("$.totalOutputTokens").value(1500));
  }
}
