package com.dongha.monitoring.project.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record ProjectBudgetRequest(
    @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}", message = "yearMonth 형식은 YYYY-MM 이어야 합니다")
        String yearMonth,
    @NotNull @DecimalMin(value = "0.0001", message = "예산은 0보다 커야 합니다")
        BigDecimal monthlyBudgetUsd) {}
