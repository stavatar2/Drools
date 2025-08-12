package com.example.sla.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sla.domain.SlaActionLog;

public interface SlaActionLogRepository extends JpaRepository<SlaActionLog, Long> {
    boolean existsByTaskIdAndAction(String taskId, String action);
}
