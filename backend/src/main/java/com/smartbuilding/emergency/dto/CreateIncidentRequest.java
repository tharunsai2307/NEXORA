package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.IncidentCategory;
import com.smartbuilding.emergency.model.IncidentSeverity;
import com.smartbuilding.emergency.model.IncidentSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateIncidentRequest {

    @NotBlank(message = "Building ID is required")
    private String buildingId;

    private String floorId;
    private String roomId;

    @NotNull(message = "Incident category is required")
    private IncidentCategory category;

    @NotNull(message = "Incident severity is required")
    private IncidentSeverity severity;

    private IncidentSource detectionSource = IncidentSource.MANUAL;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    public CreateIncidentRequest() {}

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getFloorId() { return floorId; }
    public void setFloorId(String floorId) { this.floorId = floorId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public IncidentCategory getCategory() { return category; }
    public void setCategory(IncidentCategory category) { this.category = category; }

    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }

    public IncidentSource getDetectionSource() { return detectionSource; }
    public void setDetectionSource(IncidentSource detectionSource) { this.detectionSource = detectionSource; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
