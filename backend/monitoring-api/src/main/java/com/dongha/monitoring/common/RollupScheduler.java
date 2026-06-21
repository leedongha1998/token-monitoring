package com.dongha.monitoring.common;

import com.dongha.monitoring.rollup.service.DailyRollupService;
import com.dongha.monitoring.usage.service.UsageEventService;
import java.time.Clock;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RollupScheduler {

  private static final Logger log = LoggerFactory.getLogger(RollupScheduler.class);

  private final DailyRollupService dailyRollupService;
  private final UsageEventService usageEventService;
  private final Clock clock;

  public RollupScheduler(
      DailyRollupService dailyRollupService, UsageEventService usageEventService, Clock clock) {
    this.dailyRollupService = dailyRollupService;
    this.usageEventService = usageEventService;
    this.clock = clock;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void rollupMissingOnStartup() {
    LocalDate today = LocalDate.now(clock);
    log.info("누락 DailyRollup 백필 시작: until={}", today);
    int backfilled = usageEventService.backfillMissingPromptSummaries();
    log.info("promptSummary 백필 완료: count={}", backfilled);
    dailyRollupService.rollupMissingDates(today);
    log.info("누락 DailyRollup 백필 완료");
  }

  @Scheduled(cron = "0 0 1 * * *")
  public void rollupYesterday() {
    LocalDate yesterday = LocalDate.now(clock).minusDays(1);
    log.info("DailyRollup 시작: date={}", yesterday);
    dailyRollupService.rollup(yesterday);
    log.info("DailyRollup 완료: date={}", yesterday);
  }
}
