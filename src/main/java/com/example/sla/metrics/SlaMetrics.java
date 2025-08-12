package com.example.sla.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.sla.repository.SlaInstanceRepository;

@Component
@RequiredArgsConstructor
public class SlaMetrics {
    private final MeterRegistry registry;
    private final SlaInstanceRepository repository;
    private Counter escalations;
    private DistributionSummary firstResponseLag;

    @PostConstruct
    void init() {
        Gauge.builder("sla_open_instances", () -> repository.countByStatus("OPEN")).register(registry);
        escalations = Counter.builder("sla_escalations_total").register(registry);
        firstResponseLag = DistributionSummary.builder("sla_first_response_lag_seconds").register(registry);
    }

    public Counter escalations() {
        return escalations;
    }

    public DistributionSummary firstResponseLag() {
        return firstResponseLag;
    }
}
