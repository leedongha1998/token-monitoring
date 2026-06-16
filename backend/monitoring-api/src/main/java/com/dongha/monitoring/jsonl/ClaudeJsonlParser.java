package com.dongha.monitoring.jsonl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClaudeJsonlParser {

  private static final Logger log = LoggerFactory.getLogger(ClaudeJsonlParser.class);
  private static final int MAX_PROMPT_LENGTH = 200;

  private final ObjectMapper objectMapper;

  public ClaudeJsonlParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /** 단일 라인 파싱. 기존 API 호환성 유지용. */
  public Optional<JsonlEntry> parse(String line) {
    return parseWithPrompt(line, null);
  }

  /** 여러 줄을 순서대로 처리하며 user 메시지를 다음 assistant 엔트리에 연결. */
  public List<JsonlEntry> parseLines(List<String> lines) {
    List<JsonlEntry> result = new ArrayList<>();
    String lastUserPrompt = null;
    for (String line : lines) {
      Optional<String> promptOpt = extractUserPrompt(line);
      if (promptOpt.isPresent()) {
        lastUserPrompt = promptOpt.get();
        continue;
      }
      Optional<JsonlEntry> entry = parseWithPrompt(line, lastUserPrompt);
      if (entry.isPresent()) {
        result.add(entry.get());
        lastUserPrompt = null;
      }
    }
    return result;
  }

  /** user 타입 줄에서 프롬프트 텍스트 추출. */
  public Optional<String> extractUserPrompt(String line) {
    try {
      JsonNode root = objectMapper.readTree(line);
      if (!"user".equals(root.path("type").asText())) return Optional.empty();
      JsonNode content = root.path("message").path("content");
      if (content.isTextual()) return truncate(content.asText());
      if (content.isArray()) {
        for (JsonNode block : content) {
          if ("text".equals(block.path("type").asText())) {
            String text = block.path("text").asText();
            if (!text.isBlank()) return truncate(text);
          }
        }
      }
      return Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Optional<JsonlEntry> parseWithPrompt(String line, String promptSummary) {
    try {
      JsonNode root = objectMapper.readTree(line);

      if (!"assistant".equals(root.path("type").asText())) return Optional.empty();
      if (root.path("isSidechain").asBoolean(false)) return Optional.empty();

      String uuid = root.path("uuid").asText();
      if (uuid.isBlank()) return Optional.empty();

      JsonNode message = root.path("message");
      JsonNode usage = message.path("usage");
      if (usage.isMissingNode()) return Optional.empty();

      String model = message.path("model").asText();
      if (model.isBlank()) return Optional.empty();

      // Cache tokens are billed alongside regular input tokens
      int inputTokens =
          usage.path("input_tokens").asInt(0)
              + usage.path("cache_creation_input_tokens").asInt(0)
              + usage.path("cache_read_input_tokens").asInt(0);
      int outputTokens = usage.path("output_tokens").asInt(0);

      String timestampStr = root.path("timestamp").asText();
      Instant occurredAt = timestampStr.isBlank() ? Instant.now() : Instant.parse(timestampStr);

      return Optional.of(
          new JsonlEntry(uuid, model, inputTokens, outputTokens, occurredAt, promptSummary));
    } catch (Exception e) {
      log.debug("JSONL 라인 파싱 실패: {}", e.getMessage());
      return Optional.empty();
    }
  }

  private Optional<String> truncate(String text) {
    if (text == null || text.isBlank()) return Optional.empty();
    String trimmed = text.trim();
    return Optional.of(
        trimmed.length() > MAX_PROMPT_LENGTH
            ? trimmed.substring(0, MAX_PROMPT_LENGTH) + "..."
            : trimmed);
  }
}
