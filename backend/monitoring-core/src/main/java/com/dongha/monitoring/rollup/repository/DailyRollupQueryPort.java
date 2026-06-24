package com.dongha.monitoring.rollup.repository;

import com.dongha.monitoring.rollup.domain.DailyRollup;
import java.time.LocalDate;
import java.util.List;

public interface DailyRollupQueryPort {

  List<DailyRollup> findByProjectIdAndDateBetweenExcludingSyntheticModel(
      Long projectId, LocalDate from, LocalDate to);
}
