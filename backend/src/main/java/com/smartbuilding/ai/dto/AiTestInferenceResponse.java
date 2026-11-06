package com.smartbuilding.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiTestInferenceResponse(
        String status,
        String message,
        @JsonProperty("received_payload") Map<String, Object> receivedPayload,
        @JsonProperty("echo_timestamp") Instant echoTimestamp,
        String phase,
        String service
) {}
