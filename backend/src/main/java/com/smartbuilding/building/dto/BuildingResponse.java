package com.smartbuilding.building.dto;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.BuildingStatus;
import com.smartbuilding.building.model.BuildingType;

import java.time.Instant;

public record BuildingResponse(
        String id,
        String name,
        String buildingCode,
        BuildingType buildingType,
        String address,
        long totalFloors,
        BuildingStatus status,
        String description,
        long roomCount,
        int totalCapacity,
        int currentOccupancy,
        Instant createdAt,
        Instant updatedAt
) {
    public static BuildingResponse fromEntity(Building building, long floorCount, long roomCount, int totalCapacity, int currentOccupancy) {
        return new BuildingResponse(
                building.getId(),
                building.getName(),
                building.getBuildingCode(),
                building.getBuildingType(),
                building.getAddress(),
                floorCount,
                building.getStatus(),
                building.getDescription(),
                roomCount,
                totalCapacity,
                currentOccupancy,
                building.getCreatedAt(),
                building.getUpdatedAt()
        );
    }
}
