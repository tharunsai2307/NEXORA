package com.smartbuilding.emergency.model;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.Room;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evacuation_nodes")
public class EvacuationNode {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "node_name", length = 128, nullable = false)
    private String nodeName;

    @Column(name = "node_code", length = 64, nullable = false)
    private String nodeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", length = 32, nullable = false)
    private EvacuationNodeType nodeType;

    @Column(name = "pos_x", nullable = false)
    private double posX = 0.0;

    @Column(name = "pos_y", nullable = false)
    private double posY = 0.0;

    @Column(name = "pos_z", nullable = false)
    private double posZ = 0.0;

    @Column(name = "is_safe", nullable = false)
    private boolean isSafe = true;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public EvacuationNode() {}

    public EvacuationNode(String id, Building building, Floor floor, Room room, String nodeName,
                          String nodeCode, EvacuationNodeType nodeType, double posX, double posY, double posZ) {
        this.id = id;
        this.building = building;
        this.floor = floor;
        this.room = room;
        this.nodeName = nodeName;
        this.nodeCode = nodeCode;
        this.nodeType = nodeType;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.isSafe = true;
        this.isBlocked = false;
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

    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getNodeCode() { return nodeCode; }
    public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }

    public EvacuationNodeType getNodeType() { return nodeType; }
    public void setNodeType(EvacuationNodeType nodeType) { this.nodeType = nodeType; }

    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }

    public double getPosZ() { return posZ; }
    public void setPosZ(double posZ) { this.posZ = posZ; }

    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean safe) { isSafe = safe; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

