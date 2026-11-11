package com.smartbuilding.emergency.service;

import com.smartbuilding.emergency.dto.BuildingEmergencyStatusResponse;
import com.smartbuilding.emergency.dto.EvacuationRouteResponse;
import com.smartbuilding.emergency.dto.EvacuationSessionResponse;
import com.smartbuilding.emergency.dto.IncidentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EmergencyRealtimeBroadcaster {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyRealtimeBroadcaster.class);

    private final SimpMessagingTemplate messagingTemplate;

    public EmergencyRealtimeBroadcaster(@Autowired(required = false) SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastIncidentUpdate(String buildingId, IncidentResponse incident) {
        if (messagingTemplate == null || buildingId == null || incident == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/incidents", incident);
            messagingTemplate.convertAndSend("/topic/incidents/" + incident.id(), incident);
        } catch (Exception e) {
            logger.warn("Failed to broadcast incident update: {}", e.getMessage());
        }
    }

    public void broadcastEmergencyStatus(String buildingId, BuildingEmergencyStatusResponse status) {
        if (messagingTemplate == null || buildingId == null || status == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/emergency", status);
        } catch (Exception e) {
            logger.warn("Failed to broadcast emergency status: {}", e.getMessage());
        }
    }

    public void broadcastEvacuationUpdate(String buildingId, EvacuationSessionResponse session) {
        if (messagingTemplate == null || buildingId == null || session == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/evacuation", session);
            messagingTemplate.convertAndSend("/topic/evacuations/" + session.id(), session);
        } catch (Exception e) {
            logger.warn("Failed to broadcast evacuation session update: {}", e.getMessage());
        }
    }

    public void broadcastRoutesUpdate(String buildingId, List<EvacuationRouteResponse> routes) {
        if (messagingTemplate == null || buildingId == null || routes == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/routes", routes);
        } catch (Exception e) {
            logger.warn("Failed to broadcast evacuation routes update: {}", e.getMessage());
        }
    }

    public void broadcastAlert(String buildingId, Map<String, Object> alertPayload) {
        if (messagingTemplate == null || buildingId == null || alertPayload == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/buildings/" + buildingId + "/alerts", alertPayload);
        } catch (Exception e) {
            logger.warn("Failed to broadcast emergency alert: {}", e.getMessage());
        }
    }
}
