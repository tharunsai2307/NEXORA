package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record AiTestInferenceRequest(
        @NotBlank(message = "Sensor ID is required")
        @JsonProperty("sensor_id")
        String sensorId,

        @NotBlank(message = "Reading type is required")
        @JsonProperty("reading_type")
        String readingType,

        @NotNull(message = "Reading value is required")
        Double value,

        String unit,

        Map<String, Object> metadata
) {}
