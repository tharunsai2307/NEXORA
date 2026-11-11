package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record AiModelMetadataDto(
        @JsonProperty("model_name") String modelName,
        @JsonProperty("model_version") String modelVersion,
        @JsonProperty("model_type") String modelType,
        @JsonProperty("training_dataset_size") Integer trainingDatasetSize,
        @JsonProperty("training_timestamp") String trainingTimestamp,
        @JsonProperty("performance_metrics") Map<String, Object> performanceMetrics,
        @JsonProperty("model_status") String modelStatus
) {}

