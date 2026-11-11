package com.smartbuilding.ai.service;

import com.smartbuilding.ai.dto.*;
import java.util.List;

public interface AiServiceClient {
    AiHealthDto checkHealth();
    AiInfoDto getInfo();
    AiTestInferenceResponse testInference(AiTestInferenceRequest request);
    boolean isReachable();

    AnomalyInferenceResponse detectAnomaly(AnomalyInferenceRequest request);
    OccupancyPredictionResponse predictOccupancy(OccupancyPredictionRequest request);
    RiskAssessmentResponse assessRisk(RiskAssessmentRequest request);

    List<AiModelMetadataDto> getModels();
    AiModelStatusResponse getModelStatus();
    AiModelTrainResponse trainModels(AiModelTrainRequest request);
}
