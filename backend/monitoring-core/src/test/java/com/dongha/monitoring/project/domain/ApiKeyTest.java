package com.dongha.monitoring.project.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiKeyTest {

  private final Project project = Project.create("test-project", null);

  @Test
  void create_메서드로_활성_API_키를_생성한다() {
    // when
    ApiKey apiKey = ApiKey.create(project, "secret-key-12345");

    // then
    assertThat(apiKey.getProject()).isSameAs(project);
    assertThat(apiKey.getPrefix()).isEqualTo("secret-k");
    assertThat(apiKey.isActive()).isTrue();
    assertThat(apiKey.getKeyHash()).isNotBlank();
    assertThat(apiKey.getCreatedAt()).isNotNull();
    assertThat(apiKey.getId()).isNull();
  }

  @Test
  void hashKey_는_동일_입력에_항상_동일한_64자_hex_해시를_반환한다() {
    // given
    String rawKey = "my-secret-api-key";

    // when
    String hash1 = ApiKey.hashKey(rawKey);
    String hash2 = ApiKey.hashKey(rawKey);

    // then
    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash1).hasSize(64);
    assertThat(hash1).matches("[0-9a-f]{64}");
  }

  @Test
  void matches_올바른_원시_키이면_true를_반환한다() {
    // given
    String rawKey = "original-secret-key";
    ApiKey apiKey = ApiKey.create(project, rawKey);

    // when / then
    assertThat(apiKey.matches(rawKey)).isTrue();
  }

  @Test
  void matches_다른_원시_키이면_false를_반환한다() {
    // given
    ApiKey apiKey = ApiKey.create(project, "original-secret-key");

    // when / then
    assertThat(apiKey.matches("different-key")).isFalse();
  }

  @Test
  void deactivate_호출_시_API_키가_비활성화된다() {
    // given
    ApiKey apiKey = ApiKey.create(project, "some-key-value");
    assertThat(apiKey.isActive()).isTrue();

    // when
    apiKey.deactivate();

    // then
    assertThat(apiKey.isActive()).isFalse();
  }

  @Test
  void rawKey_길이가_8자_미만이면_prefix는_전체_rawKey다() {
    // given
    String shortKey = "abc";

    // when
    ApiKey apiKey = ApiKey.create(project, shortKey);

    // then
    assertThat(apiKey.getPrefix()).isEqualTo("abc");
  }
}
