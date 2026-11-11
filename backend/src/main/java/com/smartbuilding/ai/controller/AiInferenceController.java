package com.smartbuilding.ai.controller;

import com.smartbuilding.ai.dto.*;
import com.smartbuilding.ai.model.AiPrediction;
import com.smartbuilding.ai.model.AnomalyDetection;
import com.smartbuilding.ai.model.RiskAssessment;
import com.smartbuilding.ai.service.AiInferenceOrchestratorService;
import com.smartbuilding.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
public class AiInferenceController {

    private final AiInferenceOrchestratorService aiOrchestratorService;

    public AiInferenceController(AiInferenceOrchestratorService aiOrchestratorService) {
        this.aiOrchestratorService = aiOrchestratorService;
    }

    @PostMapping("/rooms/{roomId}/analyze")
    public ResponseEntity<ApiResponse<RoomAiOverviewResponse>> analyzeRoom(@PathVariable String roomId) {
        RoomAiOverviewResponse response = aiOrchestratorService.analyzeRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("Room AI inference analysis executed successfully", response));
    }

    @PostMapping("/buildings/{buildingId}/analyze")
    public ResponseEntity<ApiResponse<BuildingAiSummaryResponse>> analyzeBuilding(@PathVariable String buildingId) {
        BuildingAiSummaryResponse response = aiOrchestratorService.analyzeBuilding(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Building fleet AI inference analysis executed successfully", response));
    }

    @GetMapping("/rooms/{roomId}/latest")
    public ResponseEntity<ApiResponse<RoomAiOverviewResponse>> getRoomAiOverview(@PathVariable String roomId) {
        RoomAiOverviewResponse response = aiOrchestratorService.getRoomAiOverview(roomId);
        return ResponseEntity.ok(ApiResponse.success("Retrieved latest room AI telemetry", response));
    }

    @GetMapping("/buildings/{buildingId}/summary")
    public ResponseEntity<ApiResponse<BuildingAiSummaryResponse>> getBuildingAiSummary(@PathVariable String buildingId) {
        BuildingAiSummaryResponse response = aiOrchestratorService.getBuildingAiSummary(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Retrieved building AI summary telemetry", response));
    }

    @GetMapping("/anomalies")
    public ResponseEntity<ApiResponse<Page<AnomalyDetection>>> getAnomalies(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String floorId,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "detectedAt"));
        Page<AnomalyDetection> results = aiOrchestratorService.getAnomalies(buildingId, floorId, roomId, severity, pageable);
        return ResponseEntity.ok(ApiResponse.success("Retrieved anomaly detections", results));
    }

    @GetMapping("/predictions")
    public ResponseEntity<ApiResponse<Page<AiPrediction>>> getPredictions(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "predictionTimestamp"));
        Page<AiPrediction> results = aiOrchestratorService.getPredictions(buildingId, roomId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Retrieved occupancy predictions", results));
    }

    @GetMapping("/risks")
    public ResponseEntity<ApiResponse<Page<RiskAssessment>>> getRisks(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "assessedAt"));
        Page<RiskAssessment> results = aiOrchestratorService.getRiskAssessments(buildingId, riskLevel, pageable);
        return ResponseEntity.ok(ApiResponse.success("Retrieved risk assessments", results));
    }

    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<AiModelMetadataDto>>> getModels() {
        List<AiModelMetadataDto> models = aiOrchestratorService.getModelMetadata();
        return ResponseEntity.ok(ApiResponse.success("Retrieved AI model metadata", models));
    }

    @GetMapping("/models/status")
    public ResponseEntity<ApiResponse<AiModelStatusResponse>> getModelStatus() {
        AiModelStatusResponse status = aiOrchestratorService.getModelStatus();
        return ResponseEntity.ok(ApiResponse.success("Retrieved AI model registry status", status));
    }

    @PostMapping("/models/train")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<AiModelTrainResponse>> trainModels(@RequestBody(required = false) AiModelTrainRequest request) {
        if (request == null) {
            request = new AiModelTrainRequest(2000, 0.05, true);
        }
        AiModelTrainResponse response = aiOrchestratorService.trainModels(request);
        return ResponseEntity.ok(ApiResponse.success("AI models retrained successfully", response));
    }
}

