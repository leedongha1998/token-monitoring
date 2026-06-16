package com.dongha.monitoring.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongha.monitoring.usage.service.UsageEventRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonlParserTest {

  private final JsonlParser parser = new JsonlParser();

  @TempDir Path tempDir;

  @Test
  void claude_code_jsonl_라인에서_UsageEventRequest_목록을_파싱한다() throws Exception {
    // given
    String line =
        "{\"type\":\"assistant\",\"message\":{\"role\":\"assistant\","
            + "\"model\":\"claude-sonnet-4-5\","
            + "\"usage\":{\"input_tokens\":1024,\"output_tokens\":512}},"
            + "\"timestamp\":\"2026-06-13T10:00:00Z\"}";
    Path file = tempDir.resolve("test.jsonl");
    Files.writeString(file, line + "\n");

    // when
    List<UsageEventRequest> results = parser.parse(file);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.get(0).model()).isEqualTo("claude-sonnet-4-5");
    assertThat(results.get(0).inputTokens()).isEqualTo(1024);
    assertThat(results.get(0).outputTokens()).isEqualTo(512);
  }

  @Test
  void usage_필드_없는_라인은_건너뛴다() throws Exception {
    // given
    String userLine = "{\"type\":\"user\",\"message\":{\"content\":\"hello\"}}";
    Path file = tempDir.resolve("test.jsonl");
    Files.writeString(file, userLine + "\n");

    // when
    List<UsageEventRequest> results = parser.parse(file);

    // then
    assertThat(results).isEmpty();
  }

  @Test
  void uuid_필드가_있으면_idempotencyKey로_사용한다() throws Exception {
    // given
    String line =
        "{\"uuid\":\"fixed-uuid-1234\",\"type\":\"assistant\","
            + "\"message\":{\"model\":\"claude-sonnet-4-6\","
            + "\"usage\":{\"input_tokens\":10,\"output_tokens\":5}},"
            + "\"timestamp\":\"2026-06-15T00:00:00Z\"}";
    Path file = tempDir.resolve("test.jsonl");
    Files.writeString(file, line + "\n");

    // when
    List<UsageEventRequest> results = parser.parse(file);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.get(0).idempotencyKey()).isEqualTo("fixed-uuid-1234");
  }

  @Test
  void 캐시_토큰을_입력_토큰에_합산한다() throws Exception {
    // given
    String line =
        "{\"uuid\":\"uuid-cache\",\"type\":\"assistant\","
            + "\"message\":{\"model\":\"claude-sonnet-4-6\","
            + "\"usage\":{\"input_tokens\":3,\"cache_creation_input_tokens\":100,"
            + "\"cache_read_input_tokens\":50,\"output_tokens\":20}},"
            + "\"timestamp\":\"2026-06-15T00:00:00Z\"}";
    Path file = tempDir.resolve("cache.jsonl");
    Files.writeString(file, line + "\n");

    // when
    List<UsageEventRequest> results = parser.parse(file);

    // then
    assertThat(results).hasSize(1);
    // 3 + 100 + 50 = 153
    assertThat(results.get(0).inputTokens()).isEqualTo(153);
  }

  @Test
  void 여러_라인이_혼재할_때_usage가_있는_라인만_파싱한다() throws Exception {
    // given
    String assistantLine =
        "{\"type\":\"assistant\",\"message\":{\"model\":\"claude-opus-4-8\","
            + "\"usage\":{\"input_tokens\":200,\"output_tokens\":100}},"
            + "\"timestamp\":\"2026-06-13T11:00:00Z\"}";
    String userLine = "{\"type\":\"user\",\"message\":{\"content\":\"test\"}}";
    Path file = tempDir.resolve("multi.jsonl");
    Files.writeString(file, assistantLine + "\n" + userLine + "\n");

    // when
    List<UsageEventRequest> results = parser.parse(file);

    // then
    assertThat(results).hasSize(1);
    assertThat(results.get(0).model()).isEqualTo("claude-opus-4-8");
    assertThat(results.get(0).inputTokens()).isEqualTo(200);
    assertThat(results.get(0).outputTokens()).isEqualTo(100);
  }
}
