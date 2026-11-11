package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OccupancyPredictionRequest(
        @JsonProperty("room_id") String roomId,
        @JsonProperty("current_occupancy") Integer currentOccupancy,
        @JsonProperty("room_type") String roomType,
        Integer capacity,
        @JsonProperty("recent_history") List<Integer> recentHistory,
        @JsonProperty("time_of_day_hour") Integer timeOfDayHour,
        @JsonProperty("day_of_week") Integer dayOfWeek,
        @JsonProperty("prediction_horizon_minutes") Integer predictionHorizonMinutes
) {}

