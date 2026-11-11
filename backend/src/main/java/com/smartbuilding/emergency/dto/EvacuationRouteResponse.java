package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.RouteStatus;

import java.time.Instant;
import java.util.List;

public record EvacuationRouteResponse(
        String id,
        String sessionId,
        String buildingId,
        String originRoomId,
        String originRoomName,
        String originNodeId,
        String destinationExitId,
        String destinationExitName,
        List<RouteNodeDto> orderedWaypoints,
        double totalDistanceMeters,
        RouteStatus routeStatus,
        String floorTransitions,
        String calculationReason,
        Instant calculatedAt
) {}
