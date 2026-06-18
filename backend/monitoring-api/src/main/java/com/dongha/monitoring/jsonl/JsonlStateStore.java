package com.dongha.monitoring.jsonl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
  private final Map<String, String> pendingPrompts;

  public JsonlStateStore(
      @Value("${jsonl.scan.state-file:${user.home}/.claude/.monitoring-state.json}")
          String stateFilePath,
      ObjectMapper objectMapper) {
    this.stateFile = Path.of(stateFilePath);
    this.objectMapper = objectMapper;
    State loaded = load();
    this.offsets = loaded.offsets();
    this.pendingPrompts = loaded.pendingPrompts();
  }

  public long getOffset(Path file) {
    return offsets.getOrDefault(file.toAbsolutePath().toString(), 0L);
  }

  public void setOffset(Path file, long offset) {
    offsets.put(file.toAbsolutePath().toString(), offset);
  }

  public String getPendingUserPrompt(Path file) {
    return pendingPrompts.get(file.toAbsolutePath().toString());
  }

  public void setPendingUserPrompt(Path file, String prompt) {
    String key = file.toAbsolutePath().toString();
    if (prompt == null) {
      pendingPrompts.remove(key);
    } else {
      pendingPrompts.put(key, prompt);
    }
  }

  public void save() {
    try {
      Files.createDirectories(stateFile.getParent());
      objectMapper.writeValue(stateFile.toFile(), new State(offsets, pendingPrompts));
    } catch (IOException e) {
      log.warn("상태 파일 저장 실패: {}", stateFile);
    }
  }

  private State load() {
    if (!Files.exists(stateFile)) return new State(new HashMap<>(), new HashMap<>());
    try {
      JsonNode root = objectMapper.readTree(stateFile.toFile());
      if (root.has("offsets")) {
        Map<String, Long> loadedOffsets =
            objectMapper.convertValue(
                root.get("offsets"), new TypeReference<Map<String, Long>>() {});
        Map<String, String> loadedPrompts =
            root.has("pendingPrompts")
                ? objectMapper.convertValue(
                    root.get("pendingPrompts"), new TypeReference<Map<String, String>>() {})
                : new HashMap<>();
        return new State(loadedOffsets, loadedPrompts);
      }
      // 구버전 포맷(flat Map<String,Long>) 마이그레이션
      Map<String, Long> legacy =
          objectMapper.convertValue(root, new TypeReference<Map<String, Long>>() {});
      return new State(legacy, new HashMap<>());
    } catch (IOException e) {
      log.warn("상태 파일 로드 실패, 초기화: {}", stateFile);
      return new State(new HashMap<>(), new HashMap<>());
    }
  }

  private record State(Map<String, Long> offsets, Map<String, String> pendingPrompts) {}
}
