package com.dongha.monitoring.rollup.repository;

import com.dongha.monitoring.rollup.domain.DailyRollup;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRollupRepository extends JpaRepository<DailyRollup, Long> {

  Optional<DailyRollup> findByProjectIdAndDateAndModel(
      Long projectId, LocalDate date, String model);

  List<DailyRollup> findByProjectIdAndDateBetween(Long projectId, LocalDate from, LocalDate to);

  List<DailyRollup> findByDateBetween(LocalDate from, LocalDate to);

  void deleteByProjectIdAndDate(Long projectId, LocalDate date);

  void deleteByDate(LocalDate date);
}
