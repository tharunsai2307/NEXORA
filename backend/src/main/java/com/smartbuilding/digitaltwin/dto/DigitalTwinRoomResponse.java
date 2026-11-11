package com.smartbuilding.digitaltwin.dto;

import java.util.Map;

public record DigitalTwinRoomResponse(
        String id,
        String floorId,
        Integer floorNumber,
        String roomName,
        String roomCode,
        String roomType,
        String description,
        int maximumCapacity,
        int currentOccupancy,
        double utilizationPercentage,
        double posX,
        double posY,
        double posZ,
        double width,
        double depth,
        double height,
        boolean active,
        String safetyStatus,
        Double riskScore,
        String riskLevel,
        String riskExplanation,
        Double predictedOccupancy,
        Boolean anomalyDetected,
        String anomalySeverity,
        Double anomalyScore,
        String anomalyExplanation,
        Map<String, Double> sensorReadings
) {}

