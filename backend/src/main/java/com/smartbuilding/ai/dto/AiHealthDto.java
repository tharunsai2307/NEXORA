package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiHealthDto(
        String status,
        String service
) {}
