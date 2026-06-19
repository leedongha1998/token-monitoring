package com.dongha.monitoring.rollup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.pricing.domain.ModelPricing;
import com.dongha.monitoring.pricing.repository.ModelPricingRepository;
import com.dongha.monitoring.rollup.domain.DailyRollup;
import com.dongha.monitoring.rollup.repository.DailyRollupRepository;
import com.dongha.monitoring.usage.domain.UsageAggregateResult;
import com.dongha.monitoring.usage.repository.UsageEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyRollupServiceTest {

  @Mock UsageEventRepository usageEventRepository;
  @Mock ModelPricingRepository modelPricingRepository;
  @Mock DailyRollupRepository dailyRollupRepository;
  @InjectMocks DailyRollupService dailyRollupService;

  @Test
  void 집계_결과와_단가로_totalCost를_계산하여_저장한다() {
    // given
    LocalDate date = LocalDate.of(2026, 6, 12);
    UsageAggregateResult agg = new UsageAggregateResult(1L, "claude-sonnet-4-5", 1000L, 500L);
    ModelPricing pricing =
        ModelPricing.of(
            "claude-sonnet-4-5",
            new BigDecimal("3.000000"),
            new BigDecimal("15.000000"),
            Instant.parse("2026-01-01T00:00:00Z"));

    when(usageEventRepository.aggregateByDateRange(any(), any())).thenReturn(List.of(agg));
    when(modelPricingRepository.findTopByModelAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            eq("claude-sonnet-4-5"), any()))
        .thenReturn(Optional.of(pricing));

    // when
    dailyRollupService.rollup(date);

    // then
    ArgumentCaptor<DailyRollup> captor = ArgumentCaptor.forClass(DailyRollup.class);
    verify(dailyRollupRepository).save(captor.capture());
    DailyRollup saved = captor.getValue();
    assertThat(saved.getProjectId()).isEqualTo(1L);
    assertThat(saved.getDate()).isEqualTo(date);
    assertThat(saved.getModel()).isEqualTo("claude-sonnet-4-5");
    assertThat(saved.getTotalInputTokens()).isEqualTo(1000L);
    assertThat(saved.getTotalOutputTokens()).isEqualTo(500L);
    // cost = (3.0 * 1000 / 1_000_000) + (15.0 * 500 / 1_000_000) = 0.003 + 0.0075 = 0.0105
    assertThat(saved.getTotalCost()).isEqualByComparingTo(new BigDecimal("0.01050000"));
  }

  @Test
  void 단가_정보가_없으면_totalCost가_0이다() {
    // given
    UsageAggregateResult agg = new UsageAggregateResult(1L, "unknown-model", 1000L, 500L);
    when(usageEventRepository.aggregateByDateRange(any(), any())).thenReturn(List.of(agg));
    when(modelPricingRepository.findTopByModelAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            any(), any()))
        .thenReturn(Optional.empty());

    // when
    dailyRollupService.rollup(LocalDate.of(2026, 6, 12));

    // then
    ArgumentCaptor<DailyRollup> captor = ArgumentCaptor.forClass(DailyRollup.class);
    verify(dailyRollupRepository).save(captor.capture());
    assertThat(captor.getValue().getTotalCost()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void 집계_결과가_없으면_저장하지_않는다() {
    // given
    when(usageEventRepository.aggregateByDateRange(any(), any())).thenReturn(List.of());

    // when
    dailyRollupService.rollup(LocalDate.of(2026, 6, 12));

    // then
    verify(dailyRollupRepository, never()).save(any());
  }

  @Test
  void rollupMissingDates_롤업이_없는_날짜만_실행한다() {
    // given
    LocalDate june10 = LocalDate.of(2026, 6, 10);
    LocalDate june11 = LocalDate.of(2026, 6, 11);
    LocalDate june12 = LocalDate.of(2026, 6, 12);
    Instant earliest = june10.atStartOfDay(ZoneOffset.UTC).toInstant();

    when(usageEventRepository.findEarliestOccurredAt()).thenReturn(Optional.of(earliest));
    when(dailyRollupRepository.findDistinctDates()).thenReturn(List.of(june11));
    when(usageEventRepository.aggregateByDateRange(any(), any())).thenReturn(List.of());

    // when
    dailyRollupService.rollupMissingDates(june12);

    // then — june11은 이미 롤업됨, june10·june12만 삭제 후 재생성
    verify(dailyRollupRepository).deleteByDate(june10);
    verify(dailyRollupRepository).deleteByDate(june12);
    verify(dailyRollupRepository, never()).deleteByDate(june11);
  }

  @Test
  void rollupMissingDates_이벤트가_없으면_아무것도_하지_않는다() {
    // given
    when(usageEventRepository.findEarliestOccurredAt()).thenReturn(Optional.empty());

    // when
    dailyRollupService.rollupMissingDates(LocalDate.of(2026, 6, 12));

    // then
    verify(usageEventRepository, never()).aggregateByDateRange(any(), any());
  }

  @Test
  void 동일_날짜_재실행시_기존_롤업을_먼저_삭제한다() {
    // given
    LocalDate date = LocalDate.of(2026, 6, 12);
    when(usageEventRepository.aggregateByDateRange(any(), any())).thenReturn(List.of());

    // when
    dailyRollupService.rollup(date);

    // then
    verify(dailyRollupRepository).deleteByDate(date);
  }
}
