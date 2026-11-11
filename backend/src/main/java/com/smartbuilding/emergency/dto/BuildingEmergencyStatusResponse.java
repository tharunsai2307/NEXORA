package com.smartbuilding.emergency.dto;

import java.util.List;

public record BuildingEmergencyStatusResponse(
        String buildingId,
        String buildingName,
        boolean emergencyActive,
        long activeIncidentCount,
        long criticalIncidentCount,
        EvacuationSessionResponse activeSession,
        int totalOccupantsAtRisk,
        int totalRoomsCleared,
        int totalRoomsEvacuating,
        int safeExitsCount,
        int blockedRoutesCount,
        List<IncidentResponse> activeIncidents
) {}
