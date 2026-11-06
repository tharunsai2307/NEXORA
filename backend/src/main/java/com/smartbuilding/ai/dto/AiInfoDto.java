package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiInfoDto(
        @JsonProperty("service_name") String serviceName,
        String version,
        @JsonProperty("python_version") String pythonVersion,
        @JsonProperty("ml_libraries") Map<String, String> mlLibraries,
        @JsonProperty("model_status") String modelStatus,
        String phase
) {}
