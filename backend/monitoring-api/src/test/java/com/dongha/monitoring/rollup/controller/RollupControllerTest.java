package com.dongha.monitoring.rollup.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongha.monitoring.project.service.ApiKeyService;
import com.dongha.monitoring.rollup.service.DailyRollupService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RollupController.class)
class RollupControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean DailyRollupService dailyRollupService;
  @MockBean ApiKeyService apiKeyService;

  @Test
  void 날짜를_지정하면_202를_반환하고_rollup을_호출한다() throws Exception {
    // when & then
    mockMvc
        .perform(post("/v1/rollup/trigger").param("date", "2026-06-13"))
        .andExpect(status().isAccepted());

    verify(dailyRollupService).rollup(eq(LocalDate.of(2026, 6, 13)));
  }

  @Test
  void date_파라미터가_없으면_400을_반환한다() throws Exception {
    mockMvc.perform(post("/v1/rollup/trigger")).andExpect(status().isBadRequest());
  }
}
