package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.EvacuationNode;
import com.smartbuilding.emergency.model.EvacuationNodeType;

public record EvacuationNodeDto(
        String id,
        String buildingId,
        String floorId,
        Integer floorNumber,
        String roomId,
        String roomName,
        String nodeName,
        String nodeCode,
        EvacuationNodeType nodeType,
        double posX,
        double posY,
        double posZ,
        boolean isSafe,
        boolean isBlocked
) {
    public static EvacuationNodeDto fromEntity(EvacuationNode entity) {
        if (entity == null) return null;
        return new EvacuationNodeDto(
                entity.getId(),
                entity.getBuilding() != null ? entity.getBuilding().getId() : null,
                entity.getFloor() != null ? entity.getFloor().getId() : null,
                entity.getFloor() != null ? entity.getFloor().getFloorNumber() : 0,
                entity.getRoom() != null ? entity.getRoom().getId() : null,
                entity.getRoom() != null ? entity.getRoom().getRoomName() : null,
                entity.getNodeName(),
                entity.getNodeCode(),
                entity.getNodeType(),
                entity.getPosX(),
                entity.getPosY(),
                entity.getPosZ(),
                entity.isSafe(),
                entity.isBlocked()
        );
    }
}
