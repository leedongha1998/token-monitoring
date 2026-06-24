package com.dongha.monitoring.rollup.repository;

import com.dongha.monitoring.rollup.domain.DailyRollup;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyRollupRepository
    extends JpaRepository<DailyRollup, Long>, DailyRollupQueryPort {

  Optional<DailyRollup> findByProjectIdAndDateAndModel(
      Long projectId, LocalDate date, String model);

  List<DailyRollup> findByProjectIdAndDateBetween(Long projectId, LocalDate from, LocalDate to);

  List<DailyRollup> findByDateBetween(LocalDate from, LocalDate to);

  @Query(
      "SELECT d FROM DailyRollup d"
          + " WHERE d.projectId = :projectId AND d.date BETWEEN :from AND :to"
          + " AND d.model <> '<synthetic>'")
  List<DailyRollup> findByProjectIdAndDateBetweenExcludingSyntheticModel(
      @Param("projectId") Long projectId, @Param("from") LocalDate from, @Param("to") LocalDate to);

  @Query(
      "SELECT d FROM DailyRollup d"
          + " WHERE d.date BETWEEN :from AND :to"
          + " AND d.model <> '<synthetic>'")
  List<DailyRollup> findByDateBetweenExcludingSyntheticModel(
      @Param("from") LocalDate from, @Param("to") LocalDate to);

  @Modifying
  @Query("DELETE FROM DailyRollup d WHERE d.projectId = :projectId AND d.date = :date")
  void deleteByProjectIdAndDate(@Param("projectId") Long projectId, @Param("date") LocalDate date);

  @Query("SELECT DISTINCT d.date FROM DailyRollup d")
  List<LocalDate> findDistinctDates();

  @Modifying
  @Query("DELETE FROM DailyRollup d WHERE d.date = :date")
  void deleteByDate(@Param("date") LocalDate date);
}
