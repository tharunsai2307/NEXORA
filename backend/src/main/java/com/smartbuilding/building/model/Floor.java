package com.smartbuilding.building.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "floors", uniqueConstraints = {
        @UniqueConstraint(name = "uq_building_floor_number", columnNames = {"building_id", "floor_number"})
})
public class Floor {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "floor_name", nullable = false)
    private String floorName;

    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "layout_width", nullable = false)
    private double layoutWidth = 60.0;

    @Column(name = "layout_depth", nullable = false)
    private double layoutDepth = 40.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FloorStatus status = FloorStatus.ACTIVE;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Floor() {}

    public Floor(String id, Building building, String floorName, int floorNumber, String description, double layoutWidth, double layoutDepth, FloorStatus status) {
        this.id = id;
        this.building = building;
        this.floorName = floorName;
        this.floorNumber = floorNumber;
        this.description = description;
        this.layoutWidth = layoutWidth;
        this.layoutDepth = layoutDepth;
        this.status = status != null ? status : FloorStatus.ACTIVE;
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

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLayoutWidth() {
        return layoutWidth;
    }

    public void setLayoutWidth(double layoutWidth) {
        this.layoutWidth = layoutWidth;
    }

    public double getLayoutDepth() {
        return layoutDepth;
    }

    public void setLayoutDepth(double layoutDepth) {
        this.layoutDepth = layoutDepth;
    }

    public FloorStatus getStatus() {
        return status;
    }

    public void setStatus(FloorStatus status) {
        this.status = status;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
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

