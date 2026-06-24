package com.dongha.monitoring.usage.service;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.pricing.service.ModelPricingResult;
import com.dongha.monitoring.pricing.service.ModelPricingService;
import com.dongha.monitoring.project.service.PageResult;
import com.dongha.monitoring.usage.domain.UsageEvent;
import com.dongha.monitoring.usage.repository.UsageEventRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsageEventService {

  private static final int MAX_BATCH_SIZE = 100;

  private final UsageEventRepository usageEventRepository;
  private final ModelPricingService modelPricingService;

  public UsageEventService(
      UsageEventRepository usageEventRepository, ModelPricingService modelPricingService) {
    this.usageEventRepository = usageEventRepository;
    this.modelPricingService = modelPricingService;
  }

  @Transactional
  public IngestStatus ingest(Long projectId, UsageEventRequest request) {
    String newPayload = buildRawPayload(request.promptSummary());
    java.util.Optional<UsageEvent> existing =
        usageEventRepository.findByIdempotencyKey(request.idempotencyKey());
    if (existing.isPresent()) {
      if (newPayload != null) existing.get().fillRawPayload(newPayload);
      return IngestStatus.DUPLICATED;
    }
    usageEventRepository.save(
        UsageEvent.create(
            projectId,
            request.idempotencyKey(),
            request.model(),
            request.inputTokens(),
            request.outputTokens(),
            request.occurredAt(),
            newPayload,
            request.sessionId()));
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
                req.occurredAt(),
                buildRawPayload(req.promptSummary()),
                req.sessionId()));
        status = IngestStatus.ACCEPTED;
        accepted++;
      }
      results.add(new IngestResult(req.idempotencyKey(), status));
    }
    return new BatchIngestResponse(accepted, duplicated, results);
  }

  public PageResult<UsageEventResult> findEvents(
      Long projectId, Instant from, Instant to, String model, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
    Page<UsageEvent> result;
    if (projectId != null && model != null) {
      result =
          usageEventRepository.findByProjectAndDateRangeAndModel(
              projectId, from, to, model, pageable);
    } else if (projectId != null) {
      result = usageEventRepository.findByProjectAndDateRange(projectId, from, to, pageable);
    } else if (model != null) {
      result = usageEventRepository.findByDateRangeAndModel(from, to, model, pageable);
    } else {
      result = usageEventRepository.findByDateRange(from, to, pageable);
    }

    Map<String, Optional<ModelPricingResult>> pricingByModel =
        result.getContent().stream()
            .map(UsageEvent::getModel)
            .distinct()
            .collect(
                Collectors.toMap(
                    m -> m,
                    m -> modelPricingService.findEffectivePricing(m, Instant.now()),
                    (a, b) -> a));

    return new PageResult<>(
        result.getContent().stream()
            .map(e -> UsageEventResult.from(e, pricingByModel.get(e.getModel())))
            .toList(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.getNumber());
  }

  public List<SessionEfficiencyResponse> getSessionEfficiency(
      Long projectId, Instant from, Instant to) {
    return usageEventRepository.findSessionEfficiency(projectId, from, to).stream()
        .map(
            r -> {
              BigDecimal costUsd =
                  modelPricingService
                      .findEffectivePricing(r.model(), r.sessionDate())
                      .map(
                          pricing ->
                              pricing
                                  .inputPricePerMToken()
                                  .multiply(BigDecimal.valueOf(r.totalInputTokens()))
                                  .divide(BigDecimal.valueOf(1_000_000), 8, RoundingMode.HALF_UP)
                                  .add(
                                      pricing
                                          .outputPricePerMToken()
                                          .multiply(BigDecimal.valueOf(r.totalOutputTokens()))
                                          .divide(
                                              BigDecimal.valueOf(1_000_000),
                                              8,
                                              RoundingMode.HALF_UP)))
                      .orElse(BigDecimal.ZERO);
              return new SessionEfficiencyResponse(
                  r.sessionId(),
                  r.sessionDate(),
                  r.model(),
                  r.totalInputTokens(),
                  r.totalOutputTokens(),
                  costUsd);
            })
        .toList();
  }

  @Transactional
  public int backfillMissingPromptSummaries() {
    List<UsageEvent> events = usageEventRepository.findByRawPayloadIsNotNull();
    List<UsageEvent> toUpdate = new ArrayList<>();
    for (UsageEvent event : events) {
      if (event.getPromptSummary() == null) {
        toUpdate.add(event);
      }
    }
    if (!toUpdate.isEmpty()) {
      usageEventRepository.saveAll(toUpdate);
    }
    return toUpdate.size();
  }

  private static String buildRawPayload(String promptSummary) {
    if (promptSummary == null || promptSummary.isBlank()) return null;
    String summary = promptSummary.trim();
    if (summary.length() > 200) summary = summary.substring(0, 200);
    String escaped =
        summary
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    return "{\"promptSummary\":\"" + escaped + "\"}";
  }
}
