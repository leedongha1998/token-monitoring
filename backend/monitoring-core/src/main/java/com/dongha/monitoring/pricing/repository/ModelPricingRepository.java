package com.dongha.monitoring.pricing.repository;

import com.dongha.monitoring.pricing.domain.ModelPricing;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelPricingRepository extends JpaRepository<ModelPricing, Long> {

  Optional<ModelPricing> findTopByModelAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
      String model, Instant at);

  List<ModelPricing> findByModelOrderByEffectiveFromDesc(String model);
}
