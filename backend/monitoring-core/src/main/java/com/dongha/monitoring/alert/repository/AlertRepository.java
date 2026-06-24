package com.dongha.monitoring.alert.repository;

import com.dongha.monitoring.alert.domain.Alert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {

  List<Alert> findByProjectIdOrderByTriggeredAtDesc(Long projectId);

  List<Alert> findByProjectIdAndIsReadFalseOrderByTriggeredAtDesc(Long projectId);

  long countByProjectIdAndIsReadFalse(Long projectId);
}
