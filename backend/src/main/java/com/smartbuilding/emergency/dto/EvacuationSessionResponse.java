package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.EvacuationSession;
import com.smartbuilding.emergency.model.EvacuationSessionStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record EvacuationSessionResponse(
        String id,
        String incidentId,
        String buildingId,
        String buildingName,
        EvacuationSessionStatus sessionStatus,
        String sessionName,
        String initiatedBy,
        String notes,
        int totalRooms,
        int clearedRooms,
        int evacuatingRooms,
        int blockedRooms,
        int totalInitialOccupants,
        int remainingOccupants,
        double progressPercentage,
        Instant startedAt,
        Instant endedAt,
        List<EvacuationRoomStatusResponse> roomStatuses
) {
    public static EvacuationSessionResponse fromEntity(EvacuationSession entity) {
        if (entity == null) return null;
        double progress = 0.0;
        if (entity.getTotalInitialOccupants() > 0) {
            int evacuated = entity.getTotalInitialOccupants() - entity.getRemainingOccupants();
            progress = Math.min(100.0, Math.max(0.0, ((double) evacuated / entity.getTotalInitialOccupants()) * 100.0));
        } else if (entity.getTotalRooms() > 0) {
            progress = ((double) entity.getClearedRooms() / entity.getTotalRooms()) * 100.0;
        }

        return new EvacuationSessionResponse(
                entity.getId(),
                entity.getIncident() != null ? entity.getIncident().getId() : null,
                entity.getBuilding() != null ? entity.getBuilding().getId() : null,
                entity.getBuilding() != null ? entity.getBuilding().getName() : null,
                entity.getSessionStatus(),
                entity.getSessionName(),
                entity.getInitiatedBy(),
                entity.getNotes(),
                entity.getTotalRooms(),
                entity.getClearedRooms(),
                entity.getEvacuatingRooms(),
                entity.getBlockedRooms(),
                entity.getTotalInitialOccupants(),
                entity.getRemainingOccupants(),
                Math.round(progress * 10.0) / 10.0,
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getRoomStatuses() != null
                        ? entity.getRoomStatuses().stream().map(EvacuationRoomStatusResponse::fromEntity).collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }
}
