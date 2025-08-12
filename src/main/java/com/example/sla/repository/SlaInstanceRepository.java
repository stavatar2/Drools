package com.example.sla.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sla.domain.SlaInstance;

public interface SlaInstanceRepository extends JpaRepository<SlaInstance, String> {
    List<SlaInstance> findByStatus(String status);
    long countByStatus(String status);
}
