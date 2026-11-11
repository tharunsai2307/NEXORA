package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record AiModelTrainResponse(
        String status,
        String message,
        @JsonProperty("trained_models") List<String> trainedModels,
        Map<String, Object> metrics,
        String timestamp
) {}

