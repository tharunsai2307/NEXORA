package com.smartbuilding.health.controller;

import com.smartbuilding.ai.dto.AiHealthDto;
import com.smartbuilding.health.dto.BackendHealthResponse;
import com.smartbuilding.health.dto.DatabaseHealthResponse;
import com.smartbuilding.health.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<BackendHealthResponse> getBackendHealth() {
        return ResponseEntity.ok(healthService.getBackendHealth());
    }

    @GetMapping("/ai")
    public ResponseEntity<AiHealthDto> getAiHealth() {
        return ResponseEntity.ok(healthService.getAiHealth());
    }

    @GetMapping("/database")
    public ResponseEntity<DatabaseHealthResponse> getDatabaseHealth() {
        return ResponseEntity.ok(healthService.getDatabaseHealth());
    }
}
