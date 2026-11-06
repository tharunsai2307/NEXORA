package com.smartbuilding.health.dto;

import java.time.Instant;

public record DatabaseHealthResponse(
        String status,
        String database,
        String message,
        Instant timestamp
) {}
