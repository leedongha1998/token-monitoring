package com.dongha.monitoring.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.domain.Project;
import com.dongha.monitoring.project.repository.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock private ProjectRepository projectRepository;
  private ProjectService service;

  @BeforeEach
  void setUp() {
    service = new ProjectService(projectRepository);
  }

  @Test
  void 프로젝트를_생성하면_저장하고_결과를_반환한다() {
    // given
    Project project = Project.create("test", "desc");
    when(projectRepository.save(any(Project.class))).thenReturn(project);

    // when
    ProjectResult result = service.createProject("test", "desc");

    // then
    assertThat(result.name()).isEqualTo("test");
    assertThat(result.active()).isTrue();
  }

  @Test
  void 이름이_비어있으면_INVALID_REQUEST_예외를_던진다() {
    assertThatThrownBy(() -> service.createProject("", "desc"))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_REQUEST);
  }

  @Test
  void 이름이_100자_초과하면_INVALID_REQUEST_예외를_던진다() {
    assertThatThrownBy(() -> service.createProject("a".repeat(101), "desc"))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_REQUEST);
  }

  @Test
  void 존재하지_않는_id로_조회하면_PROJECT_NOT_FOUND_예외를_던진다() {
    // given
    when(projectRepository.findById(99L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.findById(99L))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
  }

  @Test
  void 전체_조회시_페이지_결과를_반환한다() {
    // given
    Project project = Project.create("p1", null);
    when(projectRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(project)));

    // when
    PageResult<ProjectResult> result = service.findAll(0, 20);

    // then
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).name()).isEqualTo("p1");
    assertThat(result.totalElements()).isEqualTo(1L);
  }
}
