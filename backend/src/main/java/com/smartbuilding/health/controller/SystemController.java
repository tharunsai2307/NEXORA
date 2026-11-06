package com.smartbuilding.health.controller;

import com.smartbuilding.building.dto.DashboardStatsResponse;
import com.smartbuilding.building.service.DashboardStatsService;
import com.smartbuilding.common.ApiResponse;
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
    private final DashboardStatsService dashboardStatsService;

    public SystemController(HealthService healthService, DashboardStatsService dashboardStatsService) {
        this.healthService = healthService;
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/info")
    public ResponseEntity<SystemInfoResponse> getSystemInfo() {
        return ResponseEntity.ok(healthService.getSystemInfo());
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardStatsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved successfully", stats));
    }
}
