package com.example.sla;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaActionLogRepository extends JpaRepository<SlaActionLog, Long> {
    boolean existsByTaskIdAndAction(String taskId, String action);
}
