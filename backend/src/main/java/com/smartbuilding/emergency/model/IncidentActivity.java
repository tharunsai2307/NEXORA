package com.smartbuilding.emergency.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incident_activities")
public class IncidentActivity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "action", length = 64, nullable = false)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "performed_by", length = 64, nullable = false)
    private String performedBy;

    @Column(name = "performed_by_name", length = 128)
    private String performedByName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public IncidentActivity() {}

    public IncidentActivity(String id, Incident incident, String action, String details, String performedBy, String performedByName) {
        this.id = id;
        this.incident = incident;
        this.action = action;
        this.details = details;
        this.performedBy = performedBy != null ? performedBy : "SYSTEM";
        this.performedByName = performedByName;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

