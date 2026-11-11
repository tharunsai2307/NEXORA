package com.smartbuilding.emergency.model;

import com.smartbuilding.building.model.Building;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evacuation_sessions")
public class EvacuationSession {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", length = 32, nullable = false)
    private EvacuationSessionStatus sessionStatus = EvacuationSessionStatus.NOT_STARTED;

    @Column(name = "session_name", length = 255, nullable = false)
    private String sessionName;

    @Column(name = "initiated_by", length = 64, nullable = false)
    private String initiatedBy = "SYSTEM";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms = 0;

    @Column(name = "cleared_rooms", nullable = false)
    private int clearedRooms = 0;

    @Column(name = "evacuating_rooms", nullable = false)
    private int evacuatingRooms = 0;

    @Column(name = "blocked_rooms", nullable = false)
    private int blockedRooms = 0;

    @Column(name = "total_initial_occupants", nullable = false)
    private int totalInitialOccupants = 0;

    @Column(name = "remaining_occupants", nullable = false)
    private int remainingOccupants = 0;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvacuationRoomStatus> roomStatuses = new ArrayList<>();

    public EvacuationSession() {}

    public EvacuationSession(String id, Incident incident, Building building, String sessionName, String initiatedBy) {
        this.id = id;
        this.incident = incident;
        this.building = building;
        this.sessionName = sessionName;
        this.initiatedBy = initiatedBy != null ? initiatedBy : "SYSTEM";
        this.sessionStatus = EvacuationSessionStatus.NOT_STARTED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public EvacuationSessionStatus getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(EvacuationSessionStatus sessionStatus) { this.sessionStatus = sessionStatus; }

    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public int getClearedRooms() { return clearedRooms; }
    public void setClearedRooms(int clearedRooms) { this.clearedRooms = clearedRooms; }

    public int getEvacuatingRooms() { return evacuatingRooms; }
    public void setEvacuatingRooms(int evacuatingRooms) { this.evacuatingRooms = evacuatingRooms; }

    public int getBlockedRooms() { return blockedRooms; }
    public void setBlockedRooms(int blockedRooms) { this.blockedRooms = blockedRooms; }

    public int getTotalInitialOccupants() { return totalInitialOccupants; }
    public void setTotalInitialOccupants(int totalInitialOccupants) { this.totalInitialOccupants = totalInitialOccupants; }

    public int getRemainingOccupants() { return remainingOccupants; }
    public void setRemainingOccupants(int remainingOccupants) { this.remainingOccupants = remainingOccupants; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<EvacuationRoomStatus> getRoomStatuses() { return roomStatuses; }
    public void setRoomStatuses(List<EvacuationRoomStatus> roomStatuses) { this.roomStatuses = roomStatuses; }
}

