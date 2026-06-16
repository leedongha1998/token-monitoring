package com.dongha.monitoring.batch;

import com.dongha.monitoring.usage.service.UsageEventRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonlParser {

  private static final Pattern INPUT_TOKENS = Pattern.compile("\"input_tokens\"\\s*:\\s*(\\d+)");
  private static final Pattern OUTPUT_TOKENS = Pattern.compile("\"output_tokens\"\\s*:\\s*(\\d+)");
  private static final Pattern MODEL = Pattern.compile("\"model\"\\s*:\\s*\"(claude[^\"]+)\"");
  private static final Pattern TIMESTAMP = Pattern.compile("\"timestamp\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern UUID_FIELD = Pattern.compile("\"uuid\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern CACHE_CREATION_TOKENS =
      Pattern.compile("\"cache_creation_input_tokens\"\\s*:\\s*(\\d+)");
  private static final Pattern CACHE_READ_TOKENS =
      Pattern.compile("\"cache_read_input_tokens\"\\s*:\\s*(\\d+)");

  public List<UsageEventRequest> parse(Path file) throws IOException {
    try (var lines = Files.lines(file)) {
      return lines
          .filter(line -> line.contains("\"input_tokens\""))
          .map(JsonlParser::parseLine)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();
    }
  }

  private static Optional<UsageEventRequest> parseLine(String line) {
    Matcher inputMatcher = INPUT_TOKENS.matcher(line);
    Matcher outputMatcher = OUTPUT_TOKENS.matcher(line);
    Matcher modelMatcher = MODEL.matcher(line);
    if (!inputMatcher.find() || !outputMatcher.find() || !modelMatcher.find()) {
      return Optional.empty();
    }
    int inputTokens = Integer.parseInt(inputMatcher.group(1));
    int outputTokens = Integer.parseInt(outputMatcher.group(1));
    String model = modelMatcher.group(1);

    // Cache tokens are billed alongside regular input tokens
    Matcher cacheCreationMatcher = CACHE_CREATION_TOKENS.matcher(line);
    if (cacheCreationMatcher.find()) inputTokens += Integer.parseInt(cacheCreationMatcher.group(1));
    Matcher cacheReadMatcher = CACHE_READ_TOKENS.matcher(line);
    if (cacheReadMatcher.find()) inputTokens += Integer.parseInt(cacheReadMatcher.group(1));

    Matcher tsMatcher = TIMESTAMP.matcher(line);
    Instant occurredAt = tsMatcher.find() ? Instant.parse(tsMatcher.group(1)) : Instant.now();

    Matcher uuidMatcher = UUID_FIELD.matcher(line);
    String idempotencyKey =
        uuidMatcher.find() ? uuidMatcher.group(1) : UUID.randomUUID().toString();

    return Optional.of(
        new UsageEventRequest(idempotencyKey, model, inputTokens, outputTokens, occurredAt));
  }
}
