package com.dongha.monitoring.jsonl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClaudeJsonlParser {

  private static final Logger log = LoggerFactory.getLogger(ClaudeJsonlParser.class);

  private final ObjectMapper objectMapper;

  public ClaudeJsonlParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Optional<JsonlEntry> parse(String line) {
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

      return Optional.of(new JsonlEntry(uuid, model, inputTokens, outputTokens, occurredAt));
    } catch (Exception e) {
      log.debug("JSONL 라인 파싱 실패: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
