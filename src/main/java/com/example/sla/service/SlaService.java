package com.example.sla.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sla.domain.SlaActionLog;
import com.example.sla.domain.SlaInstance;
import com.example.sla.events.TaskEvent;
import com.example.sla.metrics.SlaMetrics;
import com.example.sla.repository.SlaActionLogRepository;
import com.example.sla.repository.SlaInstanceRepository;
@Service
@RequiredArgsConstructor
public class SlaService {
    private final SlaInstanceRepository instanceRepository;
    private final SlaActionLogRepository actionLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KieContainer kieContainer;
    private final SlaMetrics metrics;

    private final Map<String, Instant> lastEventTs = new ConcurrentHashMap<>();

    @KafkaListener(topics = "task.events", groupId = "sla-engine-mini")
    @Transactional
    public void onEvent(TaskEvent event) {
        lastEventTs.compute(event.getTaskId(), (k, v) -> {
            if (v != null && !event.getTimestamp().isAfter(v)) {
                return v;
            }
            processEvent(event);
            return event.getTimestamp();
        });
    }

    private void processEvent(TaskEvent event) {
        SlaInstance sla = instanceRepository.findById(event.getTaskId()).orElseGet(() -> createInstance(event));
        KieSession session = kieContainer.newKieSession();
        session.insert(sla);
        session.insert(event);
        session.fireAllRules();
        session.dispose();
        instanceRepository.save(sla);
        afterRuleActions(sla);
    }

    private SlaInstance createInstance(TaskEvent event) {
        SlaInstance sla = new SlaInstance();
        sla.setTaskId(event.getTaskId());
        sla.setPriority(event.getPriority());
        if ("P1".equals(event.getPriority())) {
            sla.setFirstResponseDeadline(event.getTimestamp().plus(Duration.ofMinutes(15)));
        }
        sla.setStatus("OPEN");
        return sla;
    }

    private void afterRuleActions(SlaInstance sla) {
        if ("ESCALATED".equals(sla.getStatus()) && !actionLogRepository.existsByTaskIdAndAction(sla.getTaskId(), "ESCALATED")) {
            SlaActionLog log = new SlaActionLog();
            log.setTaskId(sla.getTaskId());
            log.setAction("ESCALATED");
            log.setReason("NO_FIRST_RESPONSE");
            log.setTs(Instant.now());
            actionLogRepository.save(log);
            kafkaTemplate.send("sla.events", sla.getTaskId(), Map.of(
                    "type", "sla.escalated",
                    "taskId", sla.getTaskId(),
                    "reason", "NO_FIRST_RESPONSE",
                    "at", Instant.now().toString()
            ));
            metrics.escalations().increment();
        }
        if (sla.getFirstResponseAt() != null) {
            long lag = Math.max(0, Duration.between(sla.getFirstResponseDeadline(), sla.getFirstResponseAt()).getSeconds());
            metrics.firstResponseLag().record(lag);
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void evaluateAll() {
        instanceRepository.findByStatus("OPEN").forEach(sla -> {
            KieSession session = kieContainer.newKieSession();
            session.insert(sla);
            session.fireAllRules();
            session.dispose();
            instanceRepository.save(sla);
            afterRuleActions(sla);
        });
    }
}
