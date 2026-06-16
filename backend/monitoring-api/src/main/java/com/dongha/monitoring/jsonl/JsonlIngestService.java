package com.dongha.monitoring.jsonl;

import com.dongha.monitoring.usage.service.UsageEventRequest;
import com.dongha.monitoring.usage.service.UsageEventService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JsonlIngestService {

  private static final Logger log = LoggerFactory.getLogger(JsonlIngestService.class);

  private final UsageEventService usageEventService;
  private final ClaudeJsonlParser parser;
  private final JsonlStateStore stateStore;
  private final Path rootDir;
  private final Long defaultProjectId;
  private final boolean enabled;

  public JsonlIngestService(
      UsageEventService usageEventService,
      ClaudeJsonlParser parser,
      JsonlStateStore stateStore,
      @Value("${jsonl.scan.root-dir:${user.home}/.claude/projects}") String rootDirPath,
      @Value("${jsonl.scan.project-id:}") String projectIdStr,
      @Value("${jsonl.scan.enabled:false}") boolean enabled) {
    this.usageEventService = usageEventService;
    this.parser = parser;
    this.stateStore = stateStore;
    this.rootDir = Path.of(rootDirPath);
    this.defaultProjectId = projectIdStr.isBlank() ? null : Long.parseLong(projectIdStr);
    this.enabled = enabled;
  }

  public void scan() {
    if (!enabled) return;
    if (defaultProjectId == null) {
      log.warn("JSONL 스캔 건너뜀: jsonl.scan.project-id 미설정");
      return;
    }
    if (!Files.exists(rootDir)) {
      log.debug("JSONL 루트 디렉토리 없음: {}", rootDir);
      return;
    }

    try (Stream<Path> files = Files.walk(rootDir)) {
      files.filter(p -> p.toString().endsWith(".jsonl")).forEach(this::processFile);
    } catch (IOException e) {
      log.warn("JSONL 디렉토리 스캔 실패: {}", e.getMessage());
    }

    stateStore.save();
  }

  private void processFile(Path file) {
    long offset = stateStore.getOffset(file);
    long fileSize;
    try {
      fileSize = Files.size(file);
    } catch (IOException e) {
      return;
    }
    if (offset >= fileSize) return;

    List<String> lines = readLinesFrom(file, offset);
    int accepted = 0;
    for (String line : lines) {
      Optional<JsonlEntry> parsed = parser.parse(line);
      if (parsed.isEmpty()) continue;
      JsonlEntry entry = parsed.get();
      try {
        usageEventService.ingest(
            defaultProjectId,
            new UsageEventRequest(
                entry.idempotencyKey(),
                entry.model(),
                entry.inputTokens(),
                entry.outputTokens(),
                entry.occurredAt()));
        accepted++;
      } catch (Exception e) {
        log.warn("이벤트 수집 실패: key={}, error={}", entry.idempotencyKey(), e.getMessage());
      }
    }

    stateStore.setOffset(file, fileSize);
    if (accepted > 0) {
      log.info("JSONL 수집 완료: file={}, accepted={}", file.getFileName(), accepted);
    }
  }

  private List<String> readLinesFrom(Path file, long offset) {
    try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ);
        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8))) {
      channel.position(offset);
      return reader.lines().filter(l -> !l.isBlank()).toList();
    } catch (IOException e) {
      log.warn("JSONL 파일 읽기 실패: {} - {}", file.getFileName(), e.getMessage());
      return List.of();
    }
  }
}
