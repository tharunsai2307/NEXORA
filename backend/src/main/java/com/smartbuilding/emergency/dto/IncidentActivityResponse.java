package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.IncidentActivity;
import java.time.Instant;

public record IncidentActivityResponse(
        String id,
        String action,
        String details,
        String performedBy,
        String performedByName,
        Instant createdAt
) {
    public static IncidentActivityResponse fromEntity(IncidentActivity entity) {
        if (entity == null) return null;
        return new IncidentActivityResponse(
                entity.getId(),
                entity.getAction(),
                entity.getDetails(),
                entity.getPerformedBy(),
                entity.getPerformedByName(),
                entity.getCreatedAt()
        );
    }
}
