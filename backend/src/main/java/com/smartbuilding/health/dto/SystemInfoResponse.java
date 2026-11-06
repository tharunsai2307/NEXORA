package com.smartbuilding.health.dto;

import java.time.Instant;
import java.util.Map;

public record SystemInfoResponse(
        String applicationName,
        String applicationVersion,
        String backendStatus,
        String databaseConnectivityStatus,
        String aiServiceConnectivityStatus,
        String environment,
        Map<String, String> subsystemDetails,
        Instant timestamp
) {}
