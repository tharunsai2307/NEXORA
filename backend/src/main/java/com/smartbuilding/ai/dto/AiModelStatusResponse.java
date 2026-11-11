package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiModelStatusResponse(
        List<AiModelMetadataDto> models,
        @JsonProperty("total_models") Integer totalModels,
        @JsonProperty("system_status") String systemStatus
) {}
