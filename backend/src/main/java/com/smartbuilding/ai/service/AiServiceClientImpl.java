package com.smartbuilding.ai.service;

import com.smartbuilding.ai.dto.*;
import com.smartbuilding.common.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class AiServiceClientImpl implements AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClientImpl.class);
    private final RestClient restClient;

    public AiServiceClientImpl(RestClient aiRestClient) {
        this.restClient = aiRestClient;
    }

    @Override
    public AiHealthDto checkHealth() {
        try {
            logger.debug("Dispatching health check request to Python AI service...");
            return restClient.get()
                    .uri("/health")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AiHealthDto.class);
        } catch (Exception ex) {
            logger.warn("Failed to connect to Python AI engine: {}", ex.getMessage());
            throw new ServiceException(
                    "Python AI service is unreachable: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    @Override
    public AiInfoDto getInfo() {
        try {
            logger.debug("Requesting system information from Python AI service...");
            return restClient.get()
                    .uri("/info")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AiInfoDto.class);
        } catch (Exception ex) {
            logger.warn("Failed to fetch info from Python AI engine: {}", ex.getMessage());
            throw new ServiceException(
                    "Python AI service info check failed: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    @Override
    public AiTestInferenceResponse testInference(AiTestInferenceRequest request) {
        try {
            logger.info("Forwarding test inference request for sensor {} to Python AI engine...", request.sensorId());
            return restClient.post()
                    .uri("/api/v1/inference/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiTestInferenceResponse.class);
        } catch (Exception ex) {
            logger.error("Error executing test inference against AI engine: {}", ex.getMessage());
            throw new ServiceException(
                    "AI service test inference failed: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    @Override
    public boolean isReachable() {
        try {
            AiHealthDto health = checkHealth();
            return health != null && "UP".equalsIgnoreCase(health.status());
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public AnomalyInferenceResponse detectAnomaly(AnomalyInferenceRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/inference/anomaly")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AnomalyInferenceResponse.class);
        } catch (Exception ex) {
            logger.error("AI service anomaly inference failed for room {}: {}", request.roomId(), ex.getMessage());
            throw new ServiceException("AI anomaly detection unavailable: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public OccupancyPredictionResponse predictOccupancy(OccupancyPredictionRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/inference/occupancy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OccupancyPredictionResponse.class);
        } catch (Exception ex) {
            logger.error("AI service occupancy prediction failed for room {}: {}", request.roomId(), ex.getMessage());
            throw new ServiceException("AI occupancy prediction unavailable: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public RiskAssessmentResponse assessRisk(RiskAssessmentRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/inference/risk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RiskAssessmentResponse.class);
        } catch (Exception ex) {
            logger.error("AI service risk assessment failed for room {}: {}", request.roomId(), ex.getMessage());
            throw new ServiceException("AI risk assessment unavailable: " + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public List<AiModelMetadataDto> getModels() {
        try {
            return restClient.get()
                    .uri("/api/v1/models")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AiModelMetadataDto>>() {});
        } catch (Exception ex) {
            logger.warn("Failed to fetch models list from Python AI service: {}", ex.getMessage());
            return List.of();
        }
    }

    @Override
    public AiModelStatusResponse getModelStatus() {
        try {
            return restClient.get()
                    .uri("/api/v1/models/status")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AiModelStatusResponse.class);
        } catch (Exception ex) {
            logger.warn("Failed to fetch model status from Python AI service: {}", ex.getMessage());
            return new AiModelStatusResponse(List.of(), 0, "UNAVAILABLE");
        }
    }

    @Override
    public AiModelTrainResponse trainModels(AiModelTrainRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/models/train")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiModelTrainResponse.class);
        } catch (Exception ex) {
            logger.error("Failed to execute model training request: {}", ex.getMessage());
            throw new ServiceException("AI model training failed: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }
}
