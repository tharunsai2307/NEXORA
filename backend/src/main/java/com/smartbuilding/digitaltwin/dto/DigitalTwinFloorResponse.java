package com.smartbuilding.digitaltwin.dto;

import java.util.List;

public record DigitalTwinFloorResponse(
        String id,
        String buildingId,
        String floorName,
        int floorNumber,
        String description,
        double layoutWidth,
        double layoutDepth,
        String status,
        int roomCount,
        int totalCapacity,
        int currentOccupancy,
        List<DigitalTwinRoomResponse> rooms
) {}
