package com.smartbuilding.ai.dto;

import java.time.Instant;
import java.util.Map;

public record BuildingAiSummaryResponse(
        String buildingId,
        String buildingName,
        Integer totalRoomsAnalyzed,
        Double averageRiskScore,
        String overallRiskLevel,
        Long activeAnomaliesCount,
        Long criticalRoomsCount,
        Long highRiskRoomsCount,
        Long warningRoomsCount,
        Long normalRoomsCount,
        Map<String, Long> severityBreakdown,
        Instant lastAnalysisTimestamp
) {}

