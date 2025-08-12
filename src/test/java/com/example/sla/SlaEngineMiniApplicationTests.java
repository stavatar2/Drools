package com.example.sla;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.sla.events.TaskEvent;
import com.example.sla.service.SlaService;

@Testcontainers
@SpringBootTest
class SlaEngineMiniApplicationTests {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://test");
    }

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    SlaService slaService;

    @Test
    void escalatesWhenNoResponse() throws Exception {
        String taskId = "t1";
        TaskEvent created = new TaskEvent();
        created.setEventType("task.created");
        created.setTaskId(taskId);
        created.setPriority("P1");
        created.setTimestamp(Instant.now().minus(Duration.ofMinutes(16)));
        kafkaTemplate.send("task.events", taskId, created).get();

        // evaluate rules
        slaService.evaluateAll();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test", "true", kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaConsumer<String, Map> consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new org.springframework.kafka.support.serializer.JsonDeserializer<>(Map.class, false));
        consumer.subscribe(Collections.singleton("sla.events"));
        var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        boolean found = records.records("sla.events").stream().anyMatch(r -> "sla.escalated".equals(((Map)r.value()).get("type")));
        consumer.close();
        assertThat(found).isTrue();
    }
}
