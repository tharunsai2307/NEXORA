package com.smartbuilding.emergency.model;

import com.smartbuilding.building.model.Building;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evacuation_connections")
public class EvacuationConnection {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_node_id", nullable = false)
    private EvacuationNode fromNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_node_id", nullable = false)
    private EvacuationNode toNode;

    @Column(name = "distance_meters", nullable = false)
    private double distanceMeters = 5.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "hazard_level", length = 32, nullable = false)
    private HazardLevel hazardLevel = HazardLevel.NONE;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @Column(name = "is_accessible", nullable = false)
    private boolean isAccessible = true;

    @Column(name = "bidirectional", nullable = false)
    private boolean bidirectional = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public EvacuationConnection() {}

    public EvacuationConnection(String id, Building building, EvacuationNode fromNode, EvacuationNode toNode,
                                double distanceMeters, HazardLevel hazardLevel, boolean isBlocked,
                                boolean isAccessible, boolean bidirectional) {
        this.id = id;
        this.building = building;
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.distanceMeters = distanceMeters;
        this.hazardLevel = hazardLevel != null ? hazardLevel : HazardLevel.NONE;
        this.isBlocked = isBlocked;
        this.isAccessible = isAccessible;
        this.bidirectional = bidirectional;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public EvacuationNode getFromNode() { return fromNode; }
    public void setFromNode(EvacuationNode fromNode) { this.fromNode = fromNode; }

    public EvacuationNode getToNode() { return toNode; }
    public void setToNode(EvacuationNode toNode) { this.toNode = toNode; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public HazardLevel getHazardLevel() { return hazardLevel; }
    public void setHazardLevel(HazardLevel hazardLevel) { this.hazardLevel = hazardLevel; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public boolean isAccessible() { return isAccessible; }
    public void setAccessible(boolean accessible) { isAccessible = accessible; }

    public boolean isBidirectional() { return bidirectional; }
    public void setBidirectional(boolean bidirectional) { this.bidirectional = bidirectional; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

