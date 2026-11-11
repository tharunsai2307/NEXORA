package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.Incident;
import com.smartbuilding.emergency.model.IncidentCategory;
import com.smartbuilding.emergency.model.IncidentSeverity;
import com.smartbuilding.emergency.model.IncidentSource;
import com.smartbuilding.emergency.model.IncidentStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record IncidentResponse(
        String id,
        String referenceNumber,
        String buildingId,
        String buildingName,
        String floorId,
        String floorName,
        Integer floorNumber,
        String roomId,
        String roomName,
        String roomCode,
        IncidentCategory category,
        IncidentSeverity severity,
        IncidentStatus status,
        IncidentSource detectionSource,
        String title,
        String description,
        String assignedOfficerId,
        String assignedOfficerName,
        Instant detectedAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        Instant closedAt,
        List<IncidentActivityResponse> activities
) {
    public static IncidentResponse fromEntity(Incident entity) {
        if (entity == null) return null;
        return new IncidentResponse(
                entity.getId(),
                entity.getReferenceNumber(),
                entity.getBuilding() != null ? entity.getBuilding().getId() : null,
                entity.getBuilding() != null ? entity.getBuilding().getName() : null,
                entity.getFloor() != null ? entity.getFloor().getId() : null,
                entity.getFloor() != null ? entity.getFloor().getFloorName() : null,
                entity.getFloor() != null ? entity.getFloor().getFloorNumber() : null,
                entity.getRoom() != null ? entity.getRoom().getId() : null,
                entity.getRoom() != null ? entity.getRoom().getRoomName() : null,
                entity.getRoom() != null ? entity.getRoom().getRoomCode() : null,
                entity.getCategory(),
                entity.getSeverity(),
                entity.getStatus(),
                entity.getDetectionSource(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getAssignedOfficer() != null ? entity.getAssignedOfficer().getId() : null,
                entity.getAssignedOfficer() != null ? entity.getAssignedOfficer().getFullName() : null,
                entity.getDetectedAt(),
                entity.getAcknowledgedAt(),
                entity.getResolvedAt(),
                entity.getClosedAt(),
                entity.getActivities() != null
                        ? entity.getActivities().stream().map(IncidentActivityResponse::fromEntity).collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }
}
