package com.smartbuilding.simulation.model;

import com.smartbuilding.building.model.Building;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "simulation_sessions", indexes = {
        @Index(name = "idx_sim_sessions_bld", columnList = "building_id"),
        @Index(name = "idx_sim_sessions_status", columnList = "status")
})
public class SimulationSession {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "started_by", length = 36)
    private String startedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SessionStatus status = SessionStatus.RUNNING;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_type", nullable = false, length = 64)
    private ScenarioType scenarioType = ScenarioType.NORMAL;

    @Column(name = "simulation_interval_seconds", nullable = false)
    private int simulationIntervalSeconds = 5;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "stopped_at")
    private Instant stoppedAt;

    public SimulationSession() {
        this.id = UUID.randomUUID().toString();
    }

    public SimulationSession(Building building, String sessionName, String startedBy, ScenarioType scenarioType, int simulationIntervalSeconds) {
        this();
        this.building = building;
        this.sessionName = sessionName;
        this.startedBy = startedBy;
        this.scenarioType = scenarioType != null ? scenarioType : ScenarioType.NORMAL;
        this.simulationIntervalSeconds = simulationIntervalSeconds > 0 ? simulationIntervalSeconds : 5;
        this.status = SessionStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    // Getters and Setters

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

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getStartedBy() {
        return startedBy;
    }

    public void setStartedBy(String startedBy) {
        this.startedBy = startedBy;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType;
    }

    public int getSimulationIntervalSeconds() {
        return simulationIntervalSeconds;
    }

    public void setSimulationIntervalSeconds(int simulationIntervalSeconds) {
        this.simulationIntervalSeconds = simulationIntervalSeconds;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(Instant pausedAt) {
        this.pausedAt = pausedAt;
    }

    public Instant getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(Instant stoppedAt) {
        this.stoppedAt = stoppedAt;
    }
}
