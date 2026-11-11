package com.smartbuilding.emergency.model;

import com.smartbuilding.building.model.Building;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "emergency_alerts")
public class EmergencyAlert {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "alert_type", length = 64, nullable = false)
    private String alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 32, nullable = false)
    private IncidentSeverity severity;

    @Column(name = "headline", length = 255, nullable = false)
    private String headline;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "broadcast_at", nullable = false)
    private Instant broadcastAt = Instant.now();

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    public EmergencyAlert() {}

    public EmergencyAlert(String id, Incident incident, Building building, String alertType,
                          IncidentSeverity severity, String headline, String message) {
        this.id = id;
        this.incident = incident;
        this.building = building;
        this.alertType = alertType;
        this.severity = severity;
        this.headline = headline;
        this.message = message;
        this.active = true;
        this.broadcastAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getBroadcastAt() { return broadcastAt; }
    public void setBroadcastAt(Instant broadcastAt) { this.broadcastAt = broadcastAt; }

    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}

