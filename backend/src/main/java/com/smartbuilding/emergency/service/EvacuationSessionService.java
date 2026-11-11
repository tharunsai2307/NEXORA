package com.smartbuilding.emergency.service;

import com.smartbuilding.emergency.dto.BuildingEmergencyStatusResponse;
import com.smartbuilding.emergency.dto.EvacuationSessionResponse;
import com.smartbuilding.emergency.dto.StartEvacuationRequest;

import java.util.List;

public interface EvacuationSessionService {

    EvacuationSessionResponse startEvacuation(String buildingId, StartEvacuationRequest request, String userId, String userName);

    EvacuationSessionResponse pauseEvacuation(String sessionId, String userId, String userName);

    EvacuationSessionResponse resumeEvacuation(String sessionId, String userId, String userName);

    EvacuationSessionResponse cancelEvacuation(String sessionId, String userId, String userName);

    EvacuationSessionResponse stepEvacuationProgress(String sessionId);

    EvacuationSessionResponse getActiveSession(String buildingId);

    EvacuationSessionResponse getSession(String sessionId);

    List<EvacuationSessionResponse> getBuildingSessions(String buildingId);

    BuildingEmergencyStatusResponse getBuildingEmergencyStatus(String buildingId);

    BuildingEmergencyStatusResponse activateBuildingEmergency(String buildingId, String reason, String userId, String userName);
}
