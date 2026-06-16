package com.dongha.monitoring.jsonl;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ClaudeJsonlParserTest {

  private final ClaudeJsonlParser parser = new ClaudeJsonlParser(new ObjectMapper());

  @Test
  void 실제_Claude_Code_JSONL_라인을_파싱한다() {
    // given
    String line =
        "{\"uuid\":\"4addf8d6-8695-4c64-8cc0-ba2958a03254\","
            + "\"type\":\"assistant\","
            + "\"isSidechain\":false,"
            + "\"timestamp\":\"2026-06-15T12:09:03.524Z\","
            + "\"message\":{"
            + "\"model\":\"claude-sonnet-4-6\","
            + "\"usage\":{"
            + "\"input_tokens\":3,"
            + "\"cache_creation_input_tokens\":20335,"
            + "\"cache_read_input_tokens\":15600,"
            + "\"output_tokens\":234}}}";

    // when
    Optional<JsonlEntry> result = parser.parse(line);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().idempotencyKey()).isEqualTo("4addf8d6-8695-4c64-8cc0-ba2958a03254");
    assertThat(result.get().model()).isEqualTo("claude-sonnet-4-6");
    // 3 + 20335 + 15600 = 35938
    assertThat(result.get().inputTokens()).isEqualTo(35938);
    assertThat(result.get().outputTokens()).isEqualTo(234);
    assertThat(result.get().occurredAt()).isEqualTo(Instant.parse("2026-06-15T12:09:03.524Z"));
  }

  @Test
  void user_타입_라인은_건너뛴다() {
    // given
    String line = "{\"uuid\":\"abc\",\"type\":\"user\",\"message\":{\"content\":\"hello\"}}";

    // when
    Optional<JsonlEntry> result = parser.parse(line);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void isSidechain_true_라인은_건너뛴다() {
    // given
    String line =
        "{\"uuid\":\"abc\",\"type\":\"assistant\",\"isSidechain\":true,"
            + "\"message\":{\"model\":\"claude-sonnet-4-6\","
            + "\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}}";

    // when
    Optional<JsonlEntry> result = parser.parse(line);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void uuid_없는_라인은_건너뛴다() {
    // given
    String line =
        "{\"type\":\"assistant\","
            + "\"message\":{\"model\":\"claude-sonnet-4-6\","
            + "\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}}";

    // when
    Optional<JsonlEntry> result = parser.parse(line);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void 캐시_토큰_없는_라인도_정상_파싱한다() {
    // given
    String line =
        "{\"uuid\":\"test-uuid\","
            + "\"type\":\"assistant\","
            + "\"timestamp\":\"2026-06-15T00:00:00Z\","
            + "\"message\":{"
            + "\"model\":\"claude-haiku-4-5\","
            + "\"usage\":{\"input_tokens\":100,\"output_tokens\":50}}}";

    // when
    Optional<JsonlEntry> result = parser.parse(line);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().inputTokens()).isEqualTo(100);
    assertThat(result.get().outputTokens()).isEqualTo(50);
  }

  @Test
  void 잘못된_JSON_라인은_건너뛴다() {
    // given – when – then
    assertThat(parser.parse("not-valid-json")).isEmpty();
    assertThat(parser.parse("")).isEmpty();
  }
}
