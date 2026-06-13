package com.dongha.monitoring.common;

import com.dongha.monitoring.rollup.service.DailyRollupService;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RollupScheduler {

  private static final Logger log = LoggerFactory.getLogger(RollupScheduler.class);

  private final DailyRollupService dailyRollupService;
  private final Clock clock;

  public RollupScheduler(DailyRollupService dailyRollupService, Clock clock) {
    this.dailyRollupService = dailyRollupService;
    this.clock = clock;
  }

  @Scheduled(cron = "0 0 1 * * *")
  public void rollupYesterday() {
    LocalDate yesterday = LocalDate.now(clock).minusDays(1);
    log.info("DailyRollup 시작: date={}", yesterday);
    dailyRollupService.rollup(yesterday);
    log.info("DailyRollup 완료: date={}", yesterday);
  }
}
