package com.example.sla;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "sla_action_log")
@Data
public class SlaActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskId;
    private String action;
    private String reason;
    private Instant ts;
}
