package com.smartbuilding.health.dto;

public record BackendHealthResponse(
        String status,
        String service
) {}
