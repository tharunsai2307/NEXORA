package com.smartbuilding.health.service;

import com.smartbuilding.ai.dto.AiHealthDto;
import com.smartbuilding.health.dto.BackendHealthResponse;
import com.smartbuilding.health.dto.DatabaseHealthResponse;
import com.smartbuilding.health.dto.SystemInfoResponse;

public interface HealthService {
    BackendHealthResponse getBackendHealth();
    AiHealthDto getAiHealth();
    DatabaseHealthResponse getDatabaseHealth();
    SystemInfoResponse getSystemInfo();
}
