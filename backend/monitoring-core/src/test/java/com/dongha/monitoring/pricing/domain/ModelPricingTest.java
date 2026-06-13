package com.dongha.monitoring.pricing.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ModelPricingTest {

  @Test
  void 정적_팩토리로_ModelPricing을_생성하면_모든_필드가_올바르게_설정된다() {
    // given
    String model = "claude-sonnet-4-5";
    BigDecimal inputPrice = new BigDecimal("3.000000");
    BigDecimal outputPrice = new BigDecimal("15.000000");
    Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");

    // when
    ModelPricing pricing = ModelPricing.of(model, inputPrice, outputPrice, effectiveFrom);

    // then
    assertThat(pricing.getModel()).isEqualTo(model);
    assertThat(pricing.getInputPricePerMToken()).isEqualByComparingTo(inputPrice);
    assertThat(pricing.getOutputPricePerMToken()).isEqualByComparingTo(outputPrice);
    assertThat(pricing.getEffectiveFrom()).isEqualTo(effectiveFrom);
    assertThat(pricing.getId()).isNull();
  }

  @Test
  void 다른_effectiveFrom을_가진_두_ModelPricing은_서로_독립적이다() {
    // given
    Instant earlier = Instant.parse("2026-01-01T00:00:00Z");
    Instant later = Instant.parse("2026-06-01T00:00:00Z");

    // when
    ModelPricing old =
        ModelPricing.of(
            "claude-haiku-4-5", new BigDecimal("0.250000"), new BigDecimal("1.250000"), earlier);
    ModelPricing updated =
        ModelPricing.of(
            "claude-haiku-4-5", new BigDecimal("0.300000"), new BigDecimal("1.500000"), later);

    // then
    assertThat(old.getEffectiveFrom()).isBefore(updated.getEffectiveFrom());
    assertThat(old.getInputPricePerMToken())
        .isNotEqualByComparingTo(updated.getInputPricePerMToken());
  }
}
