package com.dongha.monitoring.pricing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongha.monitoring.AbstractIntegrationTest;
import com.dongha.monitoring.pricing.service.ModelPricingResult;
import com.dongha.monitoring.pricing.service.ModelPricingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PricingRepositoryTest extends AbstractIntegrationTest {

  @Autowired private ModelPricingService modelPricingService;

  @Test
  void 모델별_가격을_effective_from_기준으로_조회할_수_있다() {
    // given — 동일 모델에 대해 서로 다른 effective_from 날짜로 두 건의 단가 등록
    String model = "test-model-" + UUID.randomUUID().toString().substring(0, 8);
    modelPricingService.register(
        model,
        new BigDecimal("2.000000"),
        new BigDecimal("10.000000"),
        Instant.parse("2026-01-01T00:00:00Z"));
    modelPricingService.register(
        model,
        new BigDecimal("3.000000"),
        new BigDecimal("15.000000"),
        Instant.parse("2026-06-01T00:00:00Z"));

    // when / then — laterDate 이후 날짜로 조회 시 가장 최신 단가를 반환한다
    Optional<ModelPricingResult> resultAfter =
        modelPricingService.findEffectivePricing(model, Instant.parse("2026-07-01T00:00:00Z"));

    assertThat(resultAfter).isPresent();
    assertThat(resultAfter.get().inputPricePerMToken())
        .isEqualByComparingTo(new BigDecimal("3.000000"));
    assertThat(resultAfter.get().outputPricePerMToken())
        .isEqualByComparingTo(new BigDecimal("15.000000"));

    // when / then — earlierDate와 laterDate 사이 날짜로 조회 시 이전 단가를 반환한다
    Optional<ModelPricingResult> resultBetween =
        modelPricingService.findEffectivePricing(model, Instant.parse("2026-03-01T00:00:00Z"));

    assertThat(resultBetween).isPresent();
    assertThat(resultBetween.get().inputPricePerMToken())
        .isEqualByComparingTo(new BigDecimal("2.000000"));
    assertThat(resultBetween.get().outputPricePerMToken())
        .isEqualByComparingTo(new BigDecimal("10.000000"));

    // when / then — 모든 effective_from 이전 날짜로 조회 시 결과가 없다
    Optional<ModelPricingResult> resultNone =
        modelPricingService.findEffectivePricing(model, Instant.parse("2025-12-01T00:00:00Z"));

    assertThat(resultNone).isEmpty();
  }
}
