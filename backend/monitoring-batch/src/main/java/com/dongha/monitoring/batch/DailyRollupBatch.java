package com.dongha.monitoring.batch;

import com.dongha.monitoring.rollup.service.DailyRollupService;
import java.time.LocalDate;

public class DailyRollupBatch {

  private final DailyRollupService dailyRollupService;

  public DailyRollupBatch(DailyRollupService dailyRollupService) {
    this.dailyRollupService = dailyRollupService;
  }

  public void rollup(LocalDate date) {
    dailyRollupService.rollup(date);
  }
}
