package com.dongha.monitoring.pricing.service;

import com.dongha.monitoring.common.exception.BusinessException;
import com.dongha.monitoring.common.exception.ErrorCode;
import com.dongha.monitoring.pricing.domain.ModelPricing;
import com.dongha.monitoring.pricing.repository.ModelPricingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ModelPricingService {

  private final ModelPricingRepository modelPricingRepository;

  public ModelPricingService(ModelPricingRepository modelPricingRepository) {
    this.modelPricingRepository = modelPricingRepository;
  }

  @Transactional
  public ModelPricingResult register(
      String model,
      BigDecimal inputPricePerMToken,
      BigDecimal outputPricePerMToken,
      Instant effectiveFrom) {
    if (model == null || model.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    if (inputPricePerMToken == null || inputPricePerMToken.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    if (outputPricePerMToken == null || outputPricePerMToken.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    if (effectiveFrom == null) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    return ModelPricingResult.from(
        modelPricingRepository.save(
            ModelPricing.of(model, inputPricePerMToken, outputPricePerMToken, effectiveFrom)));
  }

  public List<ModelPricingResult> findByModel(String model) {
    if (model == null || model.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    return modelPricingRepository.findByModelOrderByEffectiveFromDesc(model).stream()
        .map(ModelPricingResult::from)
        .toList();
  }

  public Optional<ModelPricingResult> findEffectivePricing(String model, Instant at) {
    if (model == null || model.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }
    return modelPricingRepository
        .findTopByModelAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(model, at)
        .map(ModelPricingResult::from);
  }
}
