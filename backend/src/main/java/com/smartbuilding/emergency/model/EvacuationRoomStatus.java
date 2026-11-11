package com.smartbuilding.emergency.model;

import com.smartbuilding.building.model.Room;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evacuation_room_statuses")
public class EvacuationRoomStatus {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private EvacuationSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private RoomEvacuationState status = RoomEvacuationState.NOT_STARTED;

    @Column(name = "initial_occupancy", nullable = false)
    private int initialOccupancy = 0;

    @Column(name = "remaining_occupants", nullable = false)
    private int remainingOccupants = 0;

    @Column(name = "occupants_needing_assistance", nullable = false)
    private int occupantsNeedingAssistance = 0;

    @Column(name = "cleared_at")
    private Instant clearedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public EvacuationRoomStatus() {}

    public EvacuationRoomStatus(String id, EvacuationSession session, Room room, int initialOccupancy,
                                int occupantsNeedingAssistance) {
        this.id = id;
        this.session = session;
        this.room = room;
        this.initialOccupancy = initialOccupancy;
        this.remainingOccupants = initialOccupancy;
        this.occupantsNeedingAssistance = occupantsNeedingAssistance;
        this.status = initialOccupancy > 0 ? RoomEvacuationState.EVACUATING : RoomEvacuationState.CLEARED;
        if (initialOccupancy == 0) {
            this.clearedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public EvacuationSession getSession() { return session; }
    public void setSession(EvacuationSession session) { this.session = session; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public RoomEvacuationState getStatus() { return status; }
    public void setStatus(RoomEvacuationState status) { this.status = status; }

    public int getInitialOccupancy() { return initialOccupancy; }
    public void setInitialOccupancy(int initialOccupancy) { this.initialOccupancy = initialOccupancy; }

    public int getRemainingOccupants() { return remainingOccupants; }
    public void setRemainingOccupants(int remainingOccupants) { this.remainingOccupants = remainingOccupants; }

    public int getOccupantsNeedingAssistance() { return occupantsNeedingAssistance; }
    public void setOccupantsNeedingAssistance(int occupantsNeedingAssistance) { this.occupantsNeedingAssistance = occupantsNeedingAssistance; }

    public Instant getClearedAt() { return clearedAt; }
    public void setClearedAt(Instant clearedAt) { this.clearedAt = clearedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
