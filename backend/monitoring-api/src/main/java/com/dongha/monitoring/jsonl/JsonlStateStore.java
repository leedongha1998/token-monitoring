package com.dongha.monitoring.jsonl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JsonlStateStore {

  private static final Logger log = LoggerFactory.getLogger(JsonlStateStore.class);

  private final Path stateFile;
  private final ObjectMapper objectMapper;
  private final Map<String, Long> offsets;

  public JsonlStateStore(
      @Value("${jsonl.scan.state-file:${user.home}/.claude/.monitoring-state.json}")
          String stateFilePath,
      ObjectMapper objectMapper) {
    this.stateFile = Path.of(stateFilePath);
    this.objectMapper = objectMapper;
    this.offsets = load();
  }

  public long getOffset(Path file) {
    return offsets.getOrDefault(file.toAbsolutePath().toString(), 0L);
  }

  public void setOffset(Path file, long offset) {
    offsets.put(file.toAbsolutePath().toString(), offset);
  }

  public void save() {
    try {
      Files.createDirectories(stateFile.getParent());
      objectMapper.writeValue(stateFile.toFile(), offsets);
    } catch (IOException e) {
      log.warn("상태 파일 저장 실패: {}", stateFile);
    }
  }

  private Map<String, Long> load() {
    if (!Files.exists(stateFile)) return new HashMap<>();
    try {
      return objectMapper.readValue(stateFile.toFile(), new TypeReference<Map<String, Long>>() {});
    } catch (IOException e) {
      log.warn("상태 파일 로드 실패, 초기화: {}", stateFile);
      return new HashMap<>();
    }
  }
}
