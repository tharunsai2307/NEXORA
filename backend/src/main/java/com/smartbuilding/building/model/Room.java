package com.smartbuilding.building.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rooms", uniqueConstraints = {
        @UniqueConstraint(name = "uq_floor_room_code", columnNames = {"floor_id", "room_code"})
})
public class Room {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "room_code", nullable = false, length = 64)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 32)
    private RoomType roomType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "maximum_capacity", nullable = false)
    private int maximumCapacity;

    @Column(name = "current_occupancy", nullable = false)
    private int currentOccupancy = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "safety_status", nullable = false, length = 32)
    private SafetyStatus safetyStatus = SafetyStatus.NORMAL;

    @Column(name = "pos_x", nullable = false)
    private double posX = 0.0;

    @Column(name = "pos_y", nullable = false)
    private double posY = 0.0;

    @Column(name = "pos_z", nullable = false)
    private double posZ = 0.0;

    @Column(nullable = false)
    private double width = 10.0;

    @Column(nullable = false)
    private double depth = 8.0;

    @Column(nullable = false)
    private double height = 3.5;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Room() {}

    public Room(String id, Floor floor, String roomName, String roomCode, RoomType roomType, String description,
                int maximumCapacity, int currentOccupancy, SafetyStatus safetyStatus,
                double posX, double posY, double posZ, double width, double depth, double height, boolean active) {
        this.id = id;
        this.floor = floor;
        this.roomName = roomName;
        this.roomCode = roomCode;
        this.roomType = roomType;
        this.description = description;
        this.maximumCapacity = maximumCapacity;
        this.currentOccupancy = currentOccupancy;
        this.safetyStatus = safetyStatus != null ? safetyStatus : SafetyStatus.NORMAL;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.active = active;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(SafetyStatus safetyStatus) {
        this.safetyStatus = safetyStatus;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

