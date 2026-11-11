package com.smartbuilding.emergency.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignIncidentRequest {

    @NotBlank(message = "Officer user ID is required")
    private String userId;

    private String notes;

    public AssignIncidentRequest() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
