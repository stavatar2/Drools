package com.example.sla;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaInstanceRepository extends JpaRepository<SlaInstance, String> {
    List<SlaInstance> findByStatus(String status);
    long countByStatus(String status);
}
