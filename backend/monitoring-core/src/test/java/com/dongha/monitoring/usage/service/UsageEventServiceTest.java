package com.dongha.monitoring.usage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.project.service.PageResult;
import com.dongha.monitoring.usage.domain.UsageEvent;
import com.dongha.monitoring.usage.repository.UsageEventRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UsageEventServiceTest {

  @Mock private UsageEventRepository usageEventRepository;
  @InjectMocks private UsageEventService usageEventService;

  private static final Long PROJECT_ID = 1L;
  private static final UsageEventRequest SAMPLE_REQUEST =
      new UsageEventRequest("idem-key-1", "claude-sonnet-4-5", 100, 50, Instant.now(), null);

  @Test
  void 신규_idempotencyKey로_요청하면_저장하고_ACCEPTED를_반환한다() {
    // given
    when(usageEventRepository.existsByIdempotencyKey("idem-key-1")).thenReturn(false);
    when(usageEventRepository.save(any(UsageEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    // when
    IngestStatus status = usageEventService.ingest(PROJECT_ID, SAMPLE_REQUEST);

    // then
    assertThat(status).isEqualTo(IngestStatus.ACCEPTED);
    verify(usageEventRepository).save(any(UsageEvent.class));
  }

  @Test
  void 중복_idempotencyKey로_요청하면_저장하지_않고_DUPLICATED를_반환한다() {
    // given
    when(usageEventRepository.existsByIdempotencyKey("idem-key-1")).thenReturn(true);

    // when
    IngestStatus status = usageEventService.ingest(PROJECT_ID, SAMPLE_REQUEST);

    // then
    assertThat(status).isEqualTo(IngestStatus.DUPLICATED);
    verify(usageEventRepository, never()).save(any());
  }

  @Test
  void 배치에서_일부_중복_키가_있으면_부분_성공으로_처리한다() {
    // given
    UsageEventRequest req1 =
        new UsageEventRequest("key-1", "claude-sonnet-4-5", 10, 5, Instant.now(), null);
    UsageEventRequest req2 =
        new UsageEventRequest("key-2", "claude-sonnet-4-5", 20, 10, Instant.now(), null);
    when(usageEventRepository.existsByIdempotencyKey("key-1")).thenReturn(false);
    when(usageEventRepository.existsByIdempotencyKey("key-2")).thenReturn(true);
    when(usageEventRepository.save(any(UsageEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    // when
    BatchIngestResponse response = usageEventService.ingestBatch(PROJECT_ID, List.of(req1, req2));

    // then
    assertThat(response.accepted()).isEqualTo(1);
    assertThat(response.duplicated()).isEqualTo(1);
    assertThat(response.results()).hasSize(2);
    assertThat(response.results().get(0).status()).isEqualTo(IngestStatus.ACCEPTED);
    assertThat(response.results().get(1).status()).isEqualTo(IngestStatus.DUPLICATED);
  }

  @Test
  void 배치_이벤트가_100건_초과하면_BATCH_SIZE_EXCEEDED_예외를_던진다() {
    // given
    List<UsageEventRequest> events =
        IntStream.range(0, 101)
            .mapToObj(
                i ->
                    new UsageEventRequest(
                        "key-" + i, "claude-sonnet-4-5", 1, 1, Instant.now(), null))
            .toList();

    // when & then
    assertThatThrownBy(() -> usageEventService.ingestBatch(PROJECT_ID, events))
        .isInstanceOf(BusinessException.class)
        .extracting(e -> ((BusinessException) e).getErrorCode())
        .isEqualTo(ErrorCode.BATCH_SIZE_EXCEEDED);
  }

  @Test
  void 빈_배치_요청이면_accepted_0_duplicated_0으로_반환한다() {
    // when
    BatchIngestResponse response =
        usageEventService.ingestBatch(PROJECT_ID, Collections.emptyList());

    // then
    assertThat(response.accepted()).isEqualTo(0);
    assertThat(response.duplicated()).isEqualTo(0);
    assertThat(response.results()).isEmpty();
  }

  @Test
  void projectId_조건으로_이벤트_목록을_조회한다() {
    // given
    Instant from = Instant.parse("2026-06-01T00:00:00Z");
    Instant to = Instant.parse("2026-06-02T00:00:00Z");
    UsageEvent event =
        UsageEvent.create(PROJECT_ID, "idem-1", "claude-sonnet-4-5", 100, 50, from, null);
    Page<UsageEvent> page = new PageImpl<>(List.of(event));
    when(usageEventRepository.findByProjectAndDateRange(
            eq(PROJECT_ID), eq(from), eq(to), any(Pageable.class)))
        .thenReturn(page);

    // when
    PageResult<UsageEventResult> result = usageEventService.findEvents(PROJECT_ID, from, to, 0, 50);

    // then
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).model()).isEqualTo("claude-sonnet-4-5");
  }
}
