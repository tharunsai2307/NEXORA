package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record RiskAssessmentRequest(
        @JsonProperty("room_id") String roomId,
        Double temperature,
        Double humidity,
        Double smoke,
        @JsonProperty("air_quality") Double airQuality,
        Integer occupancy,
        Integer capacity,
        @JsonProperty("is_anomaly") Boolean isAnomaly,
        @JsonProperty("anomaly_score") Double anomalyScore
) {}

