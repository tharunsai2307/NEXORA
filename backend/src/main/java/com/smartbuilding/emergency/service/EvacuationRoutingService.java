package com.smartbuilding.emergency.service;

import com.smartbuilding.emergency.dto.EvacuationConnectionDto;
import com.smartbuilding.emergency.dto.EvacuationGraphResponse;
import com.smartbuilding.emergency.dto.EvacuationRouteResponse;

import java.util.List;

public interface EvacuationRoutingService {

    List<EvacuationRouteResponse> calculateRoutesForBuilding(String buildingId, String sessionId);

    List<EvacuationRouteResponse> getBuildingRoutes(String buildingId);

    List<EvacuationRouteResponse> getSessionRoutes(String sessionId);

    EvacuationGraphResponse getBuildingGraph(String buildingId);

    EvacuationConnectionDto toggleConnectionBlock(String connectionId);

    EvacuationGraphResponse initializeDefaultGraph(String buildingId);
}
