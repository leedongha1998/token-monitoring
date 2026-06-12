package com.dongha.monitoring.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.domain.ApiKey;
import com.dongha.monitoring.project.domain.Project;
import com.dongha.monitoring.project.repository.ApiKeyRepository;
import com.dongha.monitoring.project.repository.ProjectRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

  @Mock private ApiKeyRepository apiKeyRepository;
  @Mock private ProjectRepository projectRepository;
  private ApiKeyService service;

  private final Project project = Project.create("test-project", null);

  @BeforeEach
  void setUp() {
    service = new ApiKeyService(apiKeyRepository, projectRepository);
  }

  @Test
  void 유효한_원시_키로_검증하면_true를_반환한다() {
    // given
    String rawKey = "valid-secret-key-123";
    String hash = ApiKey.hashKey(rawKey);
    ApiKey apiKey = ApiKey.create(project, rawKey);
    when(apiKeyRepository.findByKeyHashAndActiveTrue(hash)).thenReturn(Optional.of(apiKey));

    // when
    boolean result = service.validateKey(rawKey);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void 존재하지_않는_키로_검증하면_false를_반환한다() {
    // given
    String rawKey = "nonexistent-key";
    String hash = ApiKey.hashKey(rawKey);
    when(apiKeyRepository.findByKeyHashAndActiveTrue(hash)).thenReturn(Optional.empty());

    // when
    boolean result = service.validateKey(rawKey);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void 프로젝트가_존재하면_API_키를_발급하고_결과를_반환한다() {
    // given
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));

    // when
    ApiKeyResult result = service.issueKey(1L);

    // then
    assertThat(result.rawKey()).isNotBlank();
    assertThat(result.prefix()).isNotBlank();
  }

  @Test
  void 존재하지_않는_프로젝트에_키_발급하면_PROJECT_NOT_FOUND_예외를_던진다() {
    // given
    when(projectRepository.findById(99L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.issueKey(99L))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
  }

  @Test
  void 존재하지_않는_API_키_비활성화시_API_KEY_NOT_FOUND_예외를_던진다() {
    // given
    when(apiKeyRepository.findById(99L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.deactivateKey(99L))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.API_KEY_NOT_FOUND);
  }
}
