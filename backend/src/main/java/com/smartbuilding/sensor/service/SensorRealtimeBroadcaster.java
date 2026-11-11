package com.smartbuilding.sensor.service;

import com.smartbuilding.sensor.dto.SensorReadingResponse;
import com.smartbuilding.simulation.dto.SimulationSessionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SensorRealtimeBroadcaster {

    private static final Logger logger = LoggerFactory.getLogger(SensorRealtimeBroadcaster.class);

    private final SimpMessagingTemplate messagingTemplate;

    public SensorRealtimeBroadcaster(@Autowired(required = false) SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastSensorReading(String buildingId, SensorReadingResponse reading) {
        if (messagingTemplate == null) return;
        try {
            if (buildingId != null) {
                messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/sensors", reading);
            }
            if (reading.getSensorId() != null) {
                messagingTemplate.convertAndSend("/topic/sensors/" + reading.getSensorId(), reading);
            }
        } catch (Exception e) {
            logger.warn("Failed to broadcast sensor reading via WebSocket: {}", e.getMessage());
        }
    }

    public void broadcastBatchReadings(String buildingId, List<SensorReadingResponse> readings) {
        if (messagingTemplate == null || readings == null || readings.isEmpty()) return;
        try {
            if (buildingId != null) {
                messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/sensors/batch", readings);
            }
        } catch (Exception e) {
            logger.warn("Failed to broadcast batch sensor readings: {}", e.getMessage());
        }
    }

    public void broadcastRoomOccupancy(String buildingId, String roomId, int occupancy, int maxCapacity) {
        if (messagingTemplate == null) return;
        try {
            Map<String, Object> payload = Map.of(
                    "roomId", roomId,
                    "buildingId", buildingId != null ? buildingId : "",
                    "currentOccupancy", occupancy,
                    "maximumCapacity", maxCapacity,
                    "timestamp", System.currentTimeMillis()
            );
            if (buildingId != null) {
                messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/occupancy", payload);
            }
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/occupancy", payload);
        } catch (Exception e) {
            logger.warn("Failed to broadcast room occupancy update: {}", e.getMessage());
        }
    }

    public void broadcastSessionStatus(SimulationSessionResponse session) {
        if (messagingTemplate == null || session == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/simulations/" + session.getId(), session);
            if (session.getBuildingId() != null) {
                messagingTemplate.convertAndSend("/topic/buildings/" + session.getBuildingId() + "/simulation", session);
            }
        } catch (Exception e) {
            logger.warn("Failed to broadcast simulation session status: {}", e.getMessage());
        }
    }

    public void broadcastAiAnomaly(String buildingId, Object anomalyPayload) {
        if (messagingTemplate == null || buildingId == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/ai/anomalies", anomalyPayload);
        } catch (Exception e) {
            logger.warn("Failed to broadcast AI anomaly update: {}", e.getMessage());
        }
    }

    public void broadcastAiPrediction(String buildingId, Object predictionPayload) {
        if (messagingTemplate == null || buildingId == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/ai/predictions", predictionPayload);
        } catch (Exception e) {
            logger.warn("Failed to broadcast AI occupancy prediction update: {}", e.getMessage());
        }
    }

    public void broadcastAiRisk(String buildingId, Object riskPayload) {
        if (messagingTemplate == null || buildingId == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/ai/risk", riskPayload);
        } catch (Exception e) {
            logger.warn("Failed to broadcast AI risk assessment update: {}", e.getMessage());
        }
    }
}

