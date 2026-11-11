package com.smartbuilding.emergency.model;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.user.model.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "reference_number", length = 64, nullable = false, unique = true)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 32, nullable = false)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 32, nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private IncidentStatus status = IncidentStatus.DETECTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_source", length = 32, nullable = false)
    private IncidentSource detectionSource;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedOfficer;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt = Instant.now();

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<IncidentActivity> activities = new ArrayList<>();

    public Incident() {}

    public Incident(String id, String referenceNumber, Building building, Floor floor, Room room,
                    IncidentCategory category, IncidentSeverity severity, IncidentStatus status,
                    IncidentSource detectionSource, String title, String description) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.building = building;
        this.floor = floor;
        this.room = room;
        this.category = category;
        this.severity = severity;
        this.status = status != null ? status : IncidentStatus.DETECTED;
        this.detectionSource = detectionSource;
        this.title = title;
        this.description = description;
        this.detectedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public IncidentCategory getCategory() { return category; }
    public void setCategory(IncidentCategory category) { this.category = category; }

    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }

    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }

    public IncidentSource getDetectionSource() { return detectionSource; }
    public void setDetectionSource(IncidentSource detectionSource) { this.detectionSource = detectionSource; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(User assignedOfficer) { this.assignedOfficer = assignedOfficer; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }

    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<IncidentActivity> getActivities() { return activities; }
    public void setActivities(List<IncidentActivity> activities) { this.activities = activities; }
}

