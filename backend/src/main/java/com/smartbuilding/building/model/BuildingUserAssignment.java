package com.smartbuilding.building.model;

import com.smartbuilding.user.model.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "building_user_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_building_user", columnNames = {"building_id", "user_id"})
})
public class BuildingUserAssignment {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    public BuildingUserAssignment() {}

    public BuildingUserAssignment(String id, Building building, User user) {
        this.id = id;
        this.building = building;
        this.user = user;
        this.assignedAt = Instant.now();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }
}

