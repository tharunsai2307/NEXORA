package com.smartbuilding.ai.service;

import com.smartbuilding.ai.dto.AiHealthDto;
import com.smartbuilding.ai.dto.AiInfoDto;
import com.smartbuilding.ai.dto.AiTestInferenceRequest;
import com.smartbuilding.ai.dto.AiTestInferenceResponse;
import com.smartbuilding.common.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
            logger.error("Failed to connect to Python AI engine: {}", ex.getMessage());
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
            logger.error("Failed to fetch info from Python AI engine: {}", ex.getMessage());
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
            logger.warn("AI service reachability probe returned false: {}", ex.getMessage());
            return false;
        }
    }
}
