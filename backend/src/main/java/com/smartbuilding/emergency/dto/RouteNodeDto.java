package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.EvacuationNodeType;

public record RouteNodeDto(
        String nodeId,
        String nodeName,
        EvacuationNodeType nodeType,
        int floorNumber,
        double posX,
        double posY,
        double posZ
) {}
