package com.smartbuilding.health.service;

import com.smartbuilding.ai.dto.AiHealthDto;
import com.smartbuilding.ai.service.AiServiceClient;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.health.dto.BackendHealthResponse;
import com.smartbuilding.health.dto.DatabaseHealthResponse;
import com.smartbuilding.health.dto.SystemInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class HealthServiceImpl implements HealthService {

    private static final Logger logger = LoggerFactory.getLogger(HealthServiceImpl.class);

    private final DataSource dataSource;
    private final AiServiceClient aiServiceClient;

    @Value("${spring.application.name:smart-building-backend}")
    private String applicationName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final String APP_VERSION = "1.0.0-SNAPSHOT";

    public HealthServiceImpl(DataSource dataSource, AiServiceClient aiServiceClient) {
        this.dataSource = dataSource;
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    public BackendHealthResponse getBackendHealth() {
        return new BackendHealthResponse("UP", "smart-building-backend");
    }

    @Override
    public AiHealthDto getAiHealth() {
        return aiServiceClient.checkHealth();
    }

    @Override
    public DatabaseHealthResponse getDatabaseHealth() {
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(2);
            if (valid) {
                String dbProductName = conn.getMetaData().getDatabaseProductName();
                String dbProductVersion = conn.getMetaData().getDatabaseProductVersion();
                return new DatabaseHealthResponse(
                        "UP",
                        dbProductName + " " + dbProductVersion,
                        "Database connection pool is healthy and responsive",
                        Instant.now()
                );
            } else {
                throw new ServiceException("Database connection validity check returned false", HttpStatus.SERVICE_UNAVAILABLE);
            }
        } catch (Exception ex) {
            logger.error("Database health check failed: {}", ex.getMessage());
            throw new ServiceException(
                    "Database connectivity verification failed: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    @Override
    public SystemInfoResponse getSystemInfo() {
        String dbStatus = "DOWN";
        String dbDetails = "Unavailable";
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                dbStatus = "UP";
                dbDetails = conn.getMetaData().getDatabaseProductName() + " (Connected)";
            }
        } catch (Exception e) {
            logger.warn("Database status probe in system info check failed: {}", e.getMessage());
            dbDetails = "Error: " + e.getMessage();
        }

        String aiStatus = "DOWN";
        String aiDetails = "Unreachable";
        try {
            AiHealthDto aiHealth = aiServiceClient.checkHealth();
            if (aiHealth != null && "UP".equalsIgnoreCase(aiHealth.status())) {
                aiStatus = "UP";
                aiDetails = aiHealth.service() + " (Operational)";
            }
        } catch (Exception e) {
            logger.warn("AI service status probe in system info check failed: {}", e.getMessage());
            aiDetails = "Error: " + e.getMessage();
        }

        Map<String, String> subsystemDetails = new HashMap<>();
        subsystemDetails.put("database", dbDetails);
        subsystemDetails.put("aiEngine", aiDetails);
        subsystemDetails.put("jvmVersion", System.getProperty("java.version"));
        subsystemDetails.put("osName", System.getProperty("os.name"));
        subsystemDetails.put("architecturePhase", "Phase 1 - Foundation");

        return new SystemInfoResponse(
                applicationName,
                APP_VERSION,
                "UP",
                dbStatus,
                aiStatus,
                activeProfile,
                subsystemDetails,
                Instant.now()
        );
    }
}
