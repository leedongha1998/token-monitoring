package com.dongha.monitoring.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.pricing.domain.ModelPricing;
import com.dongha.monitoring.pricing.repository.ModelPricingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelPricingServiceTest {

  @Mock ModelPricingRepository modelPricingRepository;
  @InjectMocks ModelPricingService modelPricingService;

  @Test
  void 유효한_단가를_등록하면_결과_DTO를_반환한다() {
    // given
    ModelPricing saved =
        ModelPricing.of(
            "claude-sonnet-4-5",
            new BigDecimal("3.000000"),
            new BigDecimal("15.000000"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(modelPricingRepository.save(any())).thenReturn(saved);

    // when
    ModelPricingResult result =
        modelPricingService.register(
            "claude-sonnet-4-5",
            new BigDecimal("3.000000"),
            new BigDecimal("15.000000"),
            Instant.parse("2026-01-01T00:00:00Z"));

    // then
    ArgumentCaptor<ModelPricing> captor = ArgumentCaptor.forClass(ModelPricing.class);
    verify(modelPricingRepository).save(captor.capture());
    assertThat(captor.getValue().getModel()).isEqualTo("claude-sonnet-4-5");
    assertThat(result.model()).isEqualTo("claude-sonnet-4-5");
    assertThat(result.inputPricePerMToken()).isEqualByComparingTo(new BigDecimal("3.000000"));
  }

  @Test
  void 모델명이_빈_문자열이면_등록_시_예외를_던진다() {
    assertThatThrownBy(
            () ->
                modelPricingService.register(
                    "",
                    new BigDecimal("3.0"),
                    new BigDecimal("15.0"),
                    Instant.parse("2026-01-01T00:00:00Z")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 입력_단가가_0이하면_등록_시_예외를_던진다() {
    assertThatThrownBy(
            () ->
                modelPricingService.register(
                    "claude-sonnet-4-5",
                    BigDecimal.ZERO,
                    new BigDecimal("15.0"),
                    Instant.parse("2026-01-01T00:00:00Z")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void 모델로_단가_이력을_effectiveFrom_내림차순으로_조회한다() {
    // given
    ModelPricing p1 =
        ModelPricing.of(
            "claude-sonnet-4-5",
            new BigDecimal("3.0"),
            new BigDecimal("15.0"),
            Instant.parse("2026-06-01T00:00:00Z"));
    ModelPricing p2 =
        ModelPricing.of(
            "claude-sonnet-4-5",
            new BigDecimal("2.5"),
            new BigDecimal("12.5"),
            Instant.parse("2026-01-01T00:00:00Z"));
    when(modelPricingRepository.findByModelOrderByEffectiveFromDesc("claude-sonnet-4-5"))
        .thenReturn(List.of(p1, p2));

    // when
    List<ModelPricingResult> result = modelPricingService.findByModel("claude-sonnet-4-5");

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).effectiveFrom()).isAfter(result.get(1).effectiveFrom());
  }

  @Test
  void 모델명이_빈_문자열이면_조회_시_예외를_던진다() {
    assertThatThrownBy(() -> modelPricingService.findByModel(""))
        .isInstanceOf(BusinessException.class);
  }
}
