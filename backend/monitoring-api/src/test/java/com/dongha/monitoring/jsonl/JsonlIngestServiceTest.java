package com.dongha.monitoring.jsonl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.project.service.ProjectService;
import com.dongha.monitoring.usage.service.IngestStatus;
import com.dongha.monitoring.usage.service.UsageEventRequest;
import com.dongha.monitoring.usage.service.UsageEventService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonlIngestServiceTest {

  @Mock UsageEventService usageEventService;
  @Mock ClaudeJsonlParser parser;
  @Mock JsonlStateStore stateStore;
  @Mock ProjectService projectService;

  @TempDir Path tempDir;

  @Test
  void 비활성화_상태에서는_스캔하지_않는다() {
    // given
    JsonlIngestService service =
        new JsonlIngestService(
            usageEventService, parser, stateStore, projectService, tempDir.toString(), "1", false);

    // when
    service.scan();

    // then
    verify(usageEventService, never()).ingest(any(), any());
    verify(stateStore, never()).save();
  }

  @Test
  void projectId_미설정이면_디렉토리명으로_프로젝트를_자동_감지한다() throws Exception {
    // given
    Path projectDir = tempDir.resolve("C--Workspace-my-app");
    Files.createDirectories(projectDir);
    Path file = projectDir.resolve("session.jsonl");
    Files.writeString(file, "{\"type\":\"assistant\"}\n");

    JsonlEntry entry =
        new JsonlEntry("auto-uuid", "claude-sonnet-4-6", 100, 50, Instant.now(), null);
    when(stateStore.getOffset(file)).thenReturn(0L);
    when(parser.parseLines(anyList())).thenReturn(List.of(entry));
    when(projectService.findOrCreateByDirectoryName("C--Workspace-my-app")).thenReturn(2L);
    when(usageEventService.ingest(eq(2L), any(UsageEventRequest.class)))
        .thenReturn(IngestStatus.ACCEPTED);

    JsonlIngestService service =
        new JsonlIngestService(
            usageEventService, parser, stateStore, projectService, tempDir.toString(), "", true);

    // when
    service.scan();

    // then
    verify(projectService).findOrCreateByDirectoryName("C--Workspace-my-app");
    verify(usageEventService).ingest(eq(2L), any(UsageEventRequest.class));
  }

  @Test
  void 새로운_JSONL_라인을_수집한다() throws Exception {
    // given
    String line = "{\"type\":\"assistant\",\"uuid\":\"test-uuid\"}";
    Path file = tempDir.resolve("session.jsonl");
    Files.writeString(file, line + "\n");
    long fileSize = Files.size(file);

    JsonlEntry entry =
        new JsonlEntry("test-uuid", "claude-sonnet-4-6", 100, 50, Instant.now(), null);
    when(stateStore.getOffset(file)).thenReturn(0L);
    when(parser.parseLines(anyList())).thenReturn(List.of(entry));
    when(usageEventService.ingest(eq(1L), any(UsageEventRequest.class)))
        .thenReturn(IngestStatus.ACCEPTED);

    JsonlIngestService service =
        new JsonlIngestService(
            usageEventService, parser, stateStore, projectService, tempDir.toString(), "1", true);

    // when
    service.scan();

    // then
    ArgumentCaptor<UsageEventRequest> captor = ArgumentCaptor.forClass(UsageEventRequest.class);
    verify(usageEventService).ingest(eq(1L), captor.capture());
    assertThat(captor.getValue().idempotencyKey()).isEqualTo("test-uuid");
    assertThat(captor.getValue().model()).isEqualTo("claude-sonnet-4-6");
    verify(stateStore).setOffset(file, fileSize);
    verify(stateStore).save();
  }

  @Test
  void 이미_처리된_파일은_건너뛴다() throws Exception {
    // given
    Path file = tempDir.resolve("session.jsonl");
    Files.writeString(file, "some content\n");
    long fileSize = Files.size(file);

    when(stateStore.getOffset(file)).thenReturn(fileSize);

    JsonlIngestService service =
        new JsonlIngestService(
            usageEventService, parser, stateStore, projectService, tempDir.toString(), "1", true);

    // when
    service.scan();

    // then
    verify(usageEventService, never()).ingest(any(), any());
    verify(stateStore).save();
  }

  @Test
  void 파싱_실패_라인은_건너뛰고_나머지는_처리한다() throws Exception {
    // given
    String badLine = "invalid-json";
    String goodLine = "{\"type\":\"assistant\",\"uuid\":\"good-uuid\"}";
    Path file = tempDir.resolve("session.jsonl");
    Files.writeString(file, badLine + "\n" + goodLine + "\n");

    JsonlEntry entry = new JsonlEntry("good-uuid", "claude-haiku-4-5", 10, 5, Instant.now(), null);
    when(stateStore.getOffset(file)).thenReturn(0L);
    when(parser.parseLines(anyList())).thenReturn(List.of(entry));
    when(usageEventService.ingest(eq(1L), any())).thenReturn(IngestStatus.ACCEPTED);

    JsonlIngestService service =
        new JsonlIngestService(
            usageEventService, parser, stateStore, projectService, tempDir.toString(), "1", true);

    // when
    service.scan();

    // then
    verify(usageEventService).ingest(eq(1L), any(UsageEventRequest.class));
  }
}
