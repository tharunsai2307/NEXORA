package com.smartbuilding.building.dto;

import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.model.RoomType;
import com.smartbuilding.building.model.SafetyStatus;

import java.time.Instant;

public record RoomResponse(
        String id,
        String floorId,
        int floorNumber,
        String buildingId,
        String roomName,
        String roomCode,
        RoomType roomType,
        String description,
        int maximumCapacity,
        int currentOccupancy,
        SafetyStatus safetyStatus,
        double posX,
        double posY,
        double posZ,
        double width,
        double depth,
        double height,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static RoomResponse fromEntity(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getFloor().getId(),
                room.getFloor().getFloorNumber(),
                room.getFloor().getBuilding().getId(),
                room.getRoomName(),
                room.getRoomCode(),
                room.getRoomType(),
                room.getDescription(),
                room.getMaximumCapacity(),
                room.getCurrentOccupancy(),
                room.getSafetyStatus(),
                room.getPosX(),
                room.getPosY(),
                room.getPosZ(),
                room.getWidth(),
                room.getDepth(),
                room.getHeight(),
                room.isActive(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}

