package com.smartbuilding.emergency.dto;

import com.smartbuilding.emergency.model.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateIncidentStatusRequest {

    @NotNull(message = "Target status is required")
    private IncidentStatus status;

    private String notes;

    public UpdateIncidentStatusRequest() {}

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
