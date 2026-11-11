package com.smartbuilding.emergency.service;

import com.smartbuilding.emergency.dto.AssignIncidentRequest;
import com.smartbuilding.emergency.dto.CreateIncidentRequest;
import com.smartbuilding.emergency.dto.IncidentResponse;
import com.smartbuilding.emergency.dto.UpdateIncidentStatusRequest;
import com.smartbuilding.emergency.model.IncidentCategory;
import com.smartbuilding.emergency.model.IncidentSeverity;
import com.smartbuilding.emergency.model.IncidentSource;
import com.smartbuilding.emergency.model.IncidentStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IncidentService {

    IncidentResponse createIncident(CreateIncidentRequest request, String userId, String userName);

    IncidentResponse getIncident(String id);

    Page<IncidentResponse> getFilteredIncidents(String buildingId, IncidentStatus status,
                                               IncidentSeverity severity, IncidentCategory category,
                                               int page, int size);

    List<IncidentResponse> getActiveBuildingIncidents(String buildingId);

    IncidentResponse updateIncidentStatus(String id, UpdateIncidentStatusRequest request, String userId, String userName);

    IncidentResponse assignIncident(String id, AssignIncidentRequest request, String assignedByUserId, String assignedByUserName);

    IncidentResponse acknowledgeIncident(String id, String userId, String userName);

    IncidentResponse resolveIncident(String id, String resolutionNotes, String userId, String userName);

    void processSensorOrAiDetection(String buildingId, String roomId, IncidentCategory category,
                                   IncidentSeverity severity, IncidentSource source,
                                   String title, String description);
}
