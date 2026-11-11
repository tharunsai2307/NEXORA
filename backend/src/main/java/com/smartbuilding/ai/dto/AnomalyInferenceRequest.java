package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AnomalyInferenceRequest(
        @JsonProperty("room_id") String roomId,
        Double temperature,
        Double humidity,
        Double smoke,
        @JsonProperty("air_quality") Double airQuality,
        Integer occupancy,
        Integer capacity,
        @JsonProperty("temp_recent_avg") Double tempRecentAvg,
        @JsonProperty("smoke_recent_avg") Double smokeRecentAvg,
        String timestamp
) {}
