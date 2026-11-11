package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record AiModelTrainRequest(
        @JsonProperty("synthetic_samples") Integer syntheticSamples,
        Double contamination,
        @JsonProperty("force_retrain") Boolean forceRetrain
) {}
