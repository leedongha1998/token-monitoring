package com.dongha.monitoring.usage.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UsageEventTest {

  @Test
  void getPromptSummary_compact_형식을_파싱한다() {
    // given — buildRawPayload가 생성하는 형식
    UsageEvent event =
        UsageEvent.create(
            1L, "key", "model", 10, 5, Instant.now(), "{\"promptSummary\":\"hello\"}", null);

    // when / then
    assertThat(event.getPromptSummary()).isEqualTo("hello");
  }

  @Test
  void getPromptSummary_JSONB_정규화_형식을_파싱한다() {
    // given — PostgreSQL JSONB가 콜론 뒤에 공백을 추가한 형식
    UsageEvent event =
        UsageEvent.create(
            1L, "key", "model", 10, 5, Instant.now(), "{\"promptSummary\": \"hello\"}", null);

    // when / then
    assertThat(event.getPromptSummary()).isEqualTo("hello");
  }

  @Test
  void getPromptSummary_이스케이프_문자를_올바르게_처리한다() {
    // given
    UsageEvent event =
        UsageEvent.create(
            1L,
            "key",
            "model",
            10,
            5,
            Instant.now(),
            "{\"promptSummary\": \"line1\\nline2\"}",
            null);

    // when / then
    assertThat(event.getPromptSummary()).isEqualTo("line1\nline2");
  }

  @Test
  void getPromptSummary_rawPayload가_null이면_null을_반환한다() {
    // given
    UsageEvent event = UsageEvent.create(1L, "key", "model", 10, 5, Instant.now(), null, null);

    // when / then
    assertThat(event.getPromptSummary()).isNull();
  }

  @Test
  void fillRawPayload_이미_값이_있으면_변경하지_않는다() {
    // given
    UsageEvent event =
        UsageEvent.create(
            1L, "key", "model", 10, 5, Instant.now(), "{\"promptSummary\": \"original\"}", null);

    // when
    event.fillRawPayload("{\"promptSummary\": \"new\"}");

    // then
    assertThat(event.getPromptSummary()).isEqualTo("original");
  }

  @Test
  void fillRawPayload_null인_경우에만_채운다() {
    // given
    UsageEvent event = UsageEvent.create(1L, "key", "model", 10, 5, Instant.now(), null, null);

    // when
    event.fillRawPayload("{\"promptSummary\": \"filled\"}");

    // then
    assertThat(event.getPromptSummary()).isEqualTo("filled");
  }
}
