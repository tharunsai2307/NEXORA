package com.smartbuilding.ai.service;

import com.smartbuilding.ai.dto.AiHealthDto;
import com.smartbuilding.ai.dto.AiInfoDto;
import com.smartbuilding.ai.dto.AiTestInferenceRequest;
import com.smartbuilding.ai.dto.AiTestInferenceResponse;

public interface AiServiceClient {
    AiHealthDto checkHealth();
    AiInfoDto getInfo();
    AiTestInferenceResponse testInference(AiTestInferenceRequest request);
    boolean isReachable();
}
