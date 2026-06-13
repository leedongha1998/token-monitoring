package com.dongha.monitoring.usage.service;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.usage.domain.UsageEvent;
import com.dongha.monitoring.usage.repository.UsageEventRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsageEventService {

  static final int MAX_BATCH_SIZE = 100;

  private final UsageEventRepository usageEventRepository;

  public UsageEventService(UsageEventRepository usageEventRepository) {
    this.usageEventRepository = usageEventRepository;
  }

  @Transactional
  public IngestStatus ingest(Long projectId, UsageEventRequest request) {
    if (usageEventRepository.existsByIdempotencyKey(request.idempotencyKey())) {
      return IngestStatus.DUPLICATED;
    }
    usageEventRepository.save(
        UsageEvent.create(
            projectId,
            request.idempotencyKey(),
            request.model(),
            request.inputTokens(),
            request.outputTokens(),
            request.occurredAt()));
    return IngestStatus.ACCEPTED;
  }

  @Transactional
  public BatchIngestResponse ingestBatch(Long projectId, List<UsageEventRequest> events) {
    if (events == null || events.size() > MAX_BATCH_SIZE) {
      throw new BusinessException(ErrorCode.BATCH_SIZE_EXCEEDED);
    }
    List<IngestResult> results = new ArrayList<>();
    int accepted = 0;
    int duplicated = 0;

    for (UsageEventRequest req : events) {
      IngestStatus status;
      if (usageEventRepository.existsByIdempotencyKey(req.idempotencyKey())) {
        status = IngestStatus.DUPLICATED;
        duplicated++;
      } else {
        usageEventRepository.save(
            UsageEvent.create(
                projectId,
                req.idempotencyKey(),
                req.model(),
                req.inputTokens(),
                req.outputTokens(),
                req.occurredAt()));
        status = IngestStatus.ACCEPTED;
        accepted++;
      }
      results.add(new IngestResult(req.idempotencyKey(), status));
    }
    return new BatchIngestResponse(accepted, duplicated, results);
  }
}
