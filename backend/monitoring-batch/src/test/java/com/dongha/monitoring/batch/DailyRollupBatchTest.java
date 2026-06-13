package com.dongha.monitoring.batch;

import static org.mockito.Mockito.verify;

import com.dongha.monitoring.rollup.service.DailyRollupService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyRollupBatchTest {

  @Mock DailyRollupService dailyRollupService;
  @InjectMocks DailyRollupBatch dailyRollupBatch;

  @Test
  void rollup_호출시_DailyRollupService에_위임한다() {
    // given
    LocalDate date = LocalDate.of(2026, 6, 12);

    // when
    dailyRollupBatch.rollup(date);

    // then
    verify(dailyRollupService).rollup(date);
  }
}
