package com.dongha.monitoring.pricing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "model_pricing")
public class ModelPricing {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String model;

  @Column(name = "input_price_per_m_token", nullable = false, precision = 10, scale = 6)
  private BigDecimal inputPricePerMToken;

  @Column(name = "output_price_per_m_token", nullable = false, precision = 10, scale = 6)
  private BigDecimal outputPricePerMToken;

  @Column(name = "effective_from", nullable = false)
  private Instant effectiveFrom;

  protected ModelPricing() {}

  public static ModelPricing of(
      String model,
      BigDecimal inputPricePerMToken,
      BigDecimal outputPricePerMToken,
      Instant effectiveFrom) {
    ModelPricing pricing = new ModelPricing();
    pricing.model = model;
    pricing.inputPricePerMToken = inputPricePerMToken;
    pricing.outputPricePerMToken = outputPricePerMToken;
    pricing.effectiveFrom = effectiveFrom;
    return pricing;
  }

  public Long getId() {
    return id;
  }

  public String getModel() {
    return model;
  }

  public BigDecimal getInputPricePerMToken() {
    return inputPricePerMToken;
  }

  public BigDecimal getOutputPricePerMToken() {
    return outputPricePerMToken;
  }

  public Instant getEffectiveFrom() {
    return effectiveFrom;
  }
}
