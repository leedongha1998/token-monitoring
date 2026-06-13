package com.dongha.monitoring.common;

import static org.mockito.Mockito.verify;

import com.dongha.monitoring.rollup.service.DailyRollupService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RollupSchedulerTest {

  @Mock DailyRollupService dailyRollupService;

  @Test
  void rollupYesterday_호출시_전날_날짜로_DailyRollupService에_위임한다() {
    // given
    Instant fixedInstant = Instant.parse("2026-06-14T01:00:00Z");
    Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
    RollupScheduler scheduler = new RollupScheduler(dailyRollupService, fixedClock);

    // when
    scheduler.rollupYesterday();

    // then
    verify(dailyRollupService).rollup(LocalDate.of(2026, 6, 13));
  }
}
