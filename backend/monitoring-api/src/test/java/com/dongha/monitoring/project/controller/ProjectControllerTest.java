package com.dongha.monitoring.project.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.service.ApiKeyService;
import com.dongha.monitoring.project.service.PageResult;
import com.dongha.monitoring.project.service.ProjectResult;
import com.dongha.monitoring.project.service.ProjectService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean ProjectService projectService;
  @MockBean ApiKeyService apiKeyService;

  @Test
  void 프로젝트_생성시_201과_Location_헤더를_반환한다() throws Exception {
    // given
    ProjectResult result = new ProjectResult(1L, "my-project", "desc", true, Instant.now());
    when(projectService.createProject("my-project", "desc")).thenReturn(result);

    // when & then
    mockMvc
        .perform(
            post("/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"my-project\",\"description\":\"desc\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("my-project"))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void 존재하지_않는_프로젝트_조회시_404와_PROJECT_001_코드를_반환한다() throws Exception {
    // given
    when(projectService.findById(99L))
        .thenThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // when & then
    mockMvc
        .perform(get("/v1/projects/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("PROJECT-001"));
  }

  @Test
  void 프로젝트_목록_조회시_페이징_응답을_반환한다() throws Exception {
    // given
    ProjectResult result = new ProjectResult(1L, "p1", null, true, Instant.now());
    when(projectService.findAll(anyInt(), anyInt()))
        .thenReturn(new PageResult<>(List.of(result), 1L, 1, 0));

    // when & then
    mockMvc
        .perform(get("/v1/projects"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").exists())
        .andExpect(jsonPath("$.totalPages").exists())
        .andExpect(jsonPath("$.number").exists());
  }

  @Test
  void 빈_이름으로_프로젝트_생성시_서비스_예외로_400을_반환한다() throws Exception {
    // given
    when(projectService.createProject("", "desc"))
        .thenThrow(new BusinessException(ErrorCode.INVALID_REQUEST));

    // when & then
    mockMvc
        .perform(
            post("/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"description\":\"desc\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("COMMON-001"));
  }
}
