package com.example.sla;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final SlaInstanceRepository repository;

    @PostMapping("/rules/reload")
    public ResponseEntity<Void> reload() {
        log.info("rules reloaded");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sla/{taskId}")
    public ResponseEntity<SlaInstance> get(@PathVariable String taskId) {
        return repository.findById(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
