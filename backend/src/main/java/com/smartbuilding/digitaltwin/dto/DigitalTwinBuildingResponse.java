package com.smartbuilding.digitaltwin.dto;

import java.util.List;

public record DigitalTwinBuildingResponse(
        String id,
        String name,
        String buildingCode,
        String buildingType,
        String address,
        int totalFloors,
        String status,
        int roomCount,
        int totalCapacity,
        int currentOccupancy,
        double overallRiskScore,
        String overallRiskLevel,
        long criticalRoomsCount,
        long highRiskRoomsCount,
        long warningRoomsCount,
        long normalRoomsCount,
        List<DigitalTwinFloorResponse> floors
) {}
