package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OccupancyPredictionResponse(
        @JsonProperty("room_id") String roomId,
        @JsonProperty("current_occupancy") Integer currentOccupancy,
        @JsonProperty("predicted_occupancy") Integer predictedOccupancy,
        @JsonProperty("prediction_horizon_minutes") Integer predictionHorizonMinutes,
        @JsonProperty("predicted_utilization_pct") Double predictedUtilizationPct,
        @JsonProperty("prediction_timestamp") String predictionTimestamp,
        @JsonProperty("model_version") String modelVersion,
        String status,
        @JsonProperty("confidence_score") Double confidenceScore
) {}

