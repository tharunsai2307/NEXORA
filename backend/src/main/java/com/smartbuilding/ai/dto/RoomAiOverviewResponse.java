package com.smartbuilding.ai.dto;

import java.time.Instant;
import java.util.Map;

public record RoomAiOverviewResponse(
        String roomId,
        String roomName,
        String roomNumber,
        String floorId,
        Integer floorNumber,
        String buildingId,
        Integer currentOccupancy,
        Integer capacity,
        Double currentUtilizationPct,
        Double riskScore,
        String riskLevel,
        Double predictedOccupancy,
        Double predictedUtilizationPct,
        Boolean anomalyDetected,
        String anomalySeverity,
        Double anomalyScore,
        String explanation,
        Map<String, Object> sensorSnapshots,
        Instant lastEvaluatedAt
) {}

