package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AnomalyInferenceResponse(
        @JsonProperty("room_id") String roomId,
        @JsonProperty("detection_timestamp") String detectionTimestamp,
        @JsonProperty("anomaly_detected") Boolean anomalyDetected,
        @JsonProperty("anomaly_score") Double anomalyScore,
        String severity,
        @JsonProperty("contributing_features") Map<String, Double> contributingFeatures,
        String explanation,
        @JsonProperty("model_version") String modelVersion
) {}
