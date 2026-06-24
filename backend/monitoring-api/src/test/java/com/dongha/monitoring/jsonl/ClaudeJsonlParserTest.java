package com.dongha.monitoring.jsonl;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
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

  @Test
  void user_타입_줄에서_텍스트_블록_프롬프트를_추출한다() {
    // given
    String line =
        "{\"type\":\"user\","
            + "\"message\":{\"content\":["
            + "{\"type\":\"text\",\"text\":\"파일을 읽어줘\"}"
            + "]}}";

    // when
    Optional<String> prompt = parser.extractUserPrompt(line);

    // then
    assertThat(prompt).contains("파일을 읽어줘");
  }

  @Test
  void user_타입_줄에서_문자열_content_프롬프트를_추출한다() {
    // given
    String line = "{\"type\":\"user\",\"message\":{\"content\":\"안녕하세요\"}}";

    // when
    Optional<String> prompt = parser.extractUserPrompt(line);

    // then
    assertThat(prompt).contains("안녕하세요");
  }

  @Test
  void parseLines_호출시_user_메시지가_다음_assistant_엔트리에_연결된다() {
    // given
    String userLine =
        "{\"type\":\"user\","
            + "\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"코드 리뷰해줘\"}]}}";
    String assistantLine =
        "{\"uuid\":\"uuid-1\","
            + "\"type\":\"assistant\","
            + "\"isSidechain\":false,"
            + "\"timestamp\":\"2026-06-15T10:00:00Z\","
            + "\"message\":{"
            + "\"model\":\"claude-sonnet-4-6\","
            + "\"usage\":{\"input_tokens\":50,\"output_tokens\":100}}}";

    // when
    List<JsonlEntry> entries = parser.parseLines(List.of(userLine, assistantLine));

    // then
    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).idempotencyKey()).isEqualTo("uuid-1");
    assertThat(entries.get(0).promptSummary()).isEqualTo("코드 리뷰해줘");
  }

  @Test
  void parseLines_이전_배치의_미연결_프롬프트가_다음_배치_assistant에_연결된다() {
    // given — 이전 배치에 user가 있었고, 이번 배치는 assistant만 있음
    String assistantLine =
        "{\"uuid\":\"uuid-cross\","
            + "\"type\":\"assistant\","
            + "\"isSidechain\":false,"
            + "\"timestamp\":\"2026-06-15T10:00:00Z\","
            + "\"message\":{"
            + "\"model\":\"claude-sonnet-4-6\","
            + "\"usage\":{\"input_tokens\":50,\"output_tokens\":100}}}";

    // when
    ParseLinesResult result = parser.parseLines(List.of(assistantLine), "이전 배치에서 온 프롬프트");

    // then
    assertThat(result.entries()).hasSize(1);
    assertThat(result.entries().get(0).promptSummary()).isEqualTo("이전 배치에서 온 프롬프트");
    assertThat(result.pendingUserPrompt()).isNull();
  }

  @Test
  void parseLines_배치_끝에_미연결_user_프롬프트가_남으면_pendingUserPrompt에_담긴다() {
    // given — 배치 마지막 줄이 user 메시지 (다음 배치로 이월됨)
    String userLine =
        "{\"type\":\"user\","
            + "\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"이월될 프롬프트\"}]}}";

    // when
    ParseLinesResult result = parser.parseLines(List.of(userLine), null);

    // then
    assertThat(result.entries()).isEmpty();
    assertThat(result.pendingUserPrompt()).isEqualTo("이월될 프롬프트");
  }
}
