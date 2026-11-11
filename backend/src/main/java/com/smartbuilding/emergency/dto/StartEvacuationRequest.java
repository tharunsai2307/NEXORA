package com.smartbuilding.emergency.dto;

public class StartEvacuationRequest {
    private String incidentId;
    private String sessionName;
    private String notes;

    public StartEvacuationRequest() {}

    public String getIncidentId() { return incidentId; }
    public void setIncidentId(String incidentId) { this.incidentId = incidentId; }

    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
