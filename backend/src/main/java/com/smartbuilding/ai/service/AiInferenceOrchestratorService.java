package com.smartbuilding.ai.service;

import com.smartbuilding.ai.dto.*;
import com.smartbuilding.ai.model.AiPrediction;
import com.smartbuilding.ai.model.AnomalyDetection;
import com.smartbuilding.ai.model.RiskAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AiInferenceOrchestratorService {

    RoomAiOverviewResponse analyzeRoom(String roomId);

    BuildingAiSummaryResponse analyzeBuilding(String buildingId);

    RoomAiOverviewResponse getRoomAiOverview(String roomId);

    BuildingAiSummaryResponse getBuildingAiSummary(String buildingId);

    Page<AnomalyDetection> getAnomalies(String buildingId, String floorId, String roomId, String severity, Pageable pageable);

    Page<AiPrediction> getPredictions(String buildingId, String roomId, Pageable pageable);

    Page<RiskAssessment> getRiskAssessments(String buildingId, String riskLevel, Pageable pageable);

    List<AiModelMetadataDto> getModelMetadata();

    AiModelStatusResponse getModelStatus();

    AiModelTrainResponse trainModels(AiModelTrainRequest request);
}

