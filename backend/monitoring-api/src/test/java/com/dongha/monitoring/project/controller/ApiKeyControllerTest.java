package com.dongha.monitoring.project.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.service.ApiKeyResult;
import com.dongha.monitoring.project.service.ApiKeyService;
import com.dongha.monitoring.project.service.ProjectService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiKeyController.class)
class ApiKeyControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean ApiKeyService apiKeyService;
  @MockBean ProjectService projectService;

  @Test
  void API키_발급시_201과_plainKey를_반환한다() throws Exception {
    // given
    ApiKeyResult result =
        new ApiKeyResult(1L, "abcd1234", "rawkey12345678901234567890123456", Instant.now());
    when(apiKeyService.issueKey(1L)).thenReturn(result);

    // when & then
    mockMvc
        .perform(post("/v1/projects/1/api-keys"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.plainKey").value("rawkey12345678901234567890123456"))
        .andExpect(jsonPath("$.prefix").value("abcd1234"));
  }

  @Test
  void 존재하지_않는_프로젝트에_API키_발급시_404와_PROJECT_001_코드를_반환한다() throws Exception {
    // given
    when(apiKeyService.issueKey(99L)).thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // when & then
    mockMvc
        .perform(post("/v1/projects/99/api-keys"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PROJECT-001"));
  }

  @Test
  void API키_비활성화시_204를_반환한다() throws Exception {
    mockMvc.perform(delete("/v1/api-keys/1")).andExpect(status().isNoContent());
  }

  @Test
  void 존재하지_않는_API키_비활성화시_404와_PROJECT_002_코드를_반환한다() throws Exception {
    // given
    doThrow(new BusinessException(ErrorCode.API_KEY_NOT_FOUND))
        .when(apiKeyService)
        .deactivateKey(99L);

    // when & then
    mockMvc
        .perform(delete("/v1/api-keys/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PROJECT-002"));
  }
}
