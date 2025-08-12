package com.example.sla.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "sla_instance")
@Data
public class SlaInstance {
    @Id
    private String taskId;
    private String priority;
    private Instant firstResponseDeadline;
    private Instant firstResponseAt;
    private String status;
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = Instant.now();
    }
}
