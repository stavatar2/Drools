package com.example.sla;

import java.time.Instant;
import lombok.Data;

@Data
public class TaskEvent {
    private String eventType;
    private String taskId;
    private String priority;
    private String authorType;
    private Instant timestamp;
}
