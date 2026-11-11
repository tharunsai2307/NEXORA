package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.EvacuationRoomStatus;
import com.smartbuilding.emergency.model.RoomEvacuationState;
import java.time.Instant;

public record EvacuationRoomStatusResponse(
        String id,
        String roomId,
        String roomName,
        String roomCode,
        Integer floorNumber,
        RoomEvacuationState status,
        int initialOccupancy,
        int remainingOccupants,
        int occupantsNeedingAssistance,
        Instant clearedAt,
        String notes
) {
    public static EvacuationRoomStatusResponse fromEntity(EvacuationRoomStatus entity) {
        if (entity == null) return null;
        return new EvacuationRoomStatusResponse(
                entity.getId(),
                entity.getRoom() != null ? entity.getRoom().getId() : null,
                entity.getRoom() != null ? entity.getRoom().getRoomName() : null,
                entity.getRoom() != null ? entity.getRoom().getRoomCode() : null,
                entity.getRoom() != null && entity.getRoom().getFloor() != null ? entity.getRoom().getFloor().getFloorNumber() : 0,
                entity.getStatus(),
                entity.getInitialOccupancy(),
                entity.getRemainingOccupants(),
                entity.getOccupantsNeedingAssistance(),
                entity.getClearedAt(),
                entity.getNotes()
        );
    }
}
