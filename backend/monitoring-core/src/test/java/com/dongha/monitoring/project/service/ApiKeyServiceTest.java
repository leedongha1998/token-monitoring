package com.dongha.monitoring.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.project.domain.ApiKey;
import com.dongha.monitoring.project.domain.Project;
import com.dongha.monitoring.project.repository.ApiKeyRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

  @Mock private ApiKeyRepository apiKeyRepository;
  private ApiKeyService service;

  private final Project project = Project.create("test-project", null);

  @BeforeEach
  void setUp() {
    service = new ApiKeyService(apiKeyRepository);
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
}
