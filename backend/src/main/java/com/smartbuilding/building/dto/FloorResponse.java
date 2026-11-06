package com.smartbuilding.building.dto;

import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.FloorStatus;

import java.time.Instant;

public record FloorResponse(
        String id,
        String buildingId,
        String floorName,
        int floorNumber,
        String description,
        double layoutWidth,
        double layoutDepth,
        FloorStatus status,
        long roomCount,
        int totalCapacity,
        int currentOccupancy,
        Instant createdAt,
        Instant updatedAt
) {
    public static FloorResponse fromEntity(Floor floor, long roomCount, int totalCapacity, int currentOccupancy) {
        return new FloorResponse(
                floor.getId(),
                floor.getBuilding().getId(),
                floor.getFloorName(),
                floor.getFloorNumber(),
                floor.getDescription(),
                floor.getLayoutWidth(),
                floor.getLayoutDepth(),
                floor.getStatus(),
                roomCount,
                totalCapacity,
                currentOccupancy,
                floor.getCreatedAt(),
                floor.getUpdatedAt()
        );
    }
}

