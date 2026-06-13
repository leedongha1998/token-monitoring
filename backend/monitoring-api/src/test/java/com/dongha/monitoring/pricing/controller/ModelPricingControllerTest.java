package com.dongha.monitoring.pricing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongha.monitoring.pricing.service.ModelPricingResult;
import com.dongha.monitoring.pricing.service.ModelPricingService;
import com.dongha.monitoring.project.service.ApiKeyService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ModelPricingController.class)
class ModelPricingControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean ModelPricingService modelPricingService;
  @MockBean ApiKeyService apiKeyService;

  @Test
  void 단가_등록_시_201과_Location_헤더를_반환한다() throws Exception {
    // given
    ModelPricingResult saved =
        new ModelPricingResult(
            1L,
            "claude-sonnet-4-5",
            new BigDecimal("3.000000"),
            new BigDecimal("15.000000"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(modelPricingService.register(any(), any(), any(), any())).thenReturn(saved);

    // when & then
    mockMvc
        .perform(
            post("/v1/pricing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "model": "claude-sonnet-4-5",
                      "inputPricePerMToken": 3.0,
                      "outputPricePerMToken": 15.0,
                      "effectiveFrom": "2026-01-01T00:00:00Z"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.model").value("claude-sonnet-4-5"));
  }

  @Test
  void 모델_단가_이력_조회시_200과_목록을_반환한다() throws Exception {
    // given
    ModelPricingResult p =
        new ModelPricingResult(
            1L,
            "claude-sonnet-4-5",
            new BigDecimal("3.000000"),
            new BigDecimal("15.000000"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(modelPricingService.findByModel("claude-sonnet-4-5")).thenReturn(List.of(p));

    // when & then
    mockMvc
        .perform(get("/v1/pricing").param("model", "claude-sonnet-4-5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].model").value("claude-sonnet-4-5"))
        .andExpect(jsonPath("$[0].inputPricePerMToken").value(3.0))
        .andExpect(jsonPath("$[0].effectiveFrom").value("2026-01-01T00:00:00Z"));
  }
}
