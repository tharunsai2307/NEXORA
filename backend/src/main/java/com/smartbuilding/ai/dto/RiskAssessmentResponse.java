package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record RiskAssessmentResponse(
        @JsonProperty("room_id") String roomId,
        @JsonProperty("overall_risk_score") Double overallRiskScore,
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("temperature_score") Double temperatureScore,
        @JsonProperty("smoke_score") Double smokeScore,
        @JsonProperty("air_quality_score") Double airQualityScore,
        @JsonProperty("occupancy_score") Double occupancyScore,
        @JsonProperty("anomaly_score") Double anomalyScore,
        @JsonProperty("contributing_factors") Map<String, Object> contributingFactors,
        String explanation,
        @JsonProperty("assessment_timestamp") String assessmentTimestamp,
        @JsonProperty("critical_override") Boolean criticalOverride
) {}
