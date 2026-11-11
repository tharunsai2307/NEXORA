package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.EvacuationConnection;
import com.smartbuilding.emergency.model.HazardLevel;

public record EvacuationConnectionDto(
        String id,
        String buildingId,
        String fromNodeId,
        String fromNodeName,
        String toNodeId,
        String toNodeName,
        double distanceMeters,
        HazardLevel hazardLevel,
        boolean isBlocked,
        boolean isAccessible,
        boolean bidirectional
) {
    public static EvacuationConnectionDto fromEntity(EvacuationConnection entity) {
        if (entity == null) return null;
        return new EvacuationConnectionDto(
                entity.getId(),
                entity.getBuilding() != null ? entity.getBuilding().getId() : null,
                entity.getFromNode() != null ? entity.getFromNode().getId() : null,
                entity.getFromNode() != null ? entity.getFromNode().getNodeName() : null,
                entity.getToNode() != null ? entity.getToNode().getId() : null,
                entity.getToNode() != null ? entity.getToNode().getNodeName() : null,
                entity.getDistanceMeters(),
                entity.getHazardLevel(),
                entity.isBlocked(),
                entity.isAccessible(),
                entity.isBidirectional()
        );
    }
}
