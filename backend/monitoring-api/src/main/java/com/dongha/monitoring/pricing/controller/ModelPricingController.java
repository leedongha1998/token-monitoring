package com.dongha.monitoring.pricing.controller;

import com.dongha.monitoring.pricing.service.ModelPricingResult;
import com.dongha.monitoring.pricing.service.ModelPricingService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/pricing")
public class ModelPricingController {

  private final ModelPricingService modelPricingService;

  public ModelPricingController(ModelPricingService modelPricingService) {
    this.modelPricingService = modelPricingService;
  }

  @PostMapping
  public ResponseEntity<ModelPricingResponse> register(@RequestBody ModelPricingRequest request) {
    ModelPricingResult result =
        modelPricingService.register(
            request.model(),
            request.inputPricePerMToken(),
            request.outputPricePerMToken(),
            request.effectiveFrom());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("?model={model}")
            .buildAndExpand(result.model())
            .toUri();
    return ResponseEntity.created(location).body(ModelPricingResponse.from(result));
  }

  @GetMapping
  public ResponseEntity<List<ModelPricingResponse>> list(@RequestParam String model) {
    List<ModelPricingResponse> results =
        modelPricingService.findByModel(model).stream().map(ModelPricingResponse::from).toList();
    return ResponseEntity.ok(results);
  }
}
