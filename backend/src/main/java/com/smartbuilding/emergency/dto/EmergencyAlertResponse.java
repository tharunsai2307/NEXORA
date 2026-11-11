package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.EmergencyAlert;
import com.smartbuilding.emergency.model.IncidentSeverity;
import java.time.Instant;

public record EmergencyAlertResponse(
        String id,
        String incidentId,
        String buildingId,
        String alertType,
        IncidentSeverity severity,
        String headline,
        String message,
        boolean active,
        Instant broadcastAt
) {
    public static EmergencyAlertResponse fromEntity(EmergencyAlert entity) {
        if (entity == null) return null;
        return new EmergencyAlertResponse(
                entity.getId(),
                entity.getIncident() != null ? entity.getIncident().getId() : null,
                entity.getBuilding() != null ? entity.getBuilding().getId() : null,
                entity.getAlertType(),
                entity.getSeverity(),
                entity.getHeadline(),
                entity.getMessage(),
                entity.isActive(),
                entity.getBroadcastAt()
        );
    }
}
