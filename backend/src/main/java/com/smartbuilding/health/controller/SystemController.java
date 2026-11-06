package com.smartbuilding.health.controller;

import com.smartbuilding.health.dto.SystemInfoResponse;
import com.smartbuilding.health.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final HealthService healthService;

    public SystemController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/info")
    public ResponseEntity<SystemInfoResponse> getSystemInfo() {
        return ResponseEntity.ok(healthService.getSystemInfo());
    }
}
