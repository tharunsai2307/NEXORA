package com.smartbuilding.simulation.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "simulation_scenarios", indexes = {
        @Index(name = "idx_sim_scenarios_session", columnList = "session_id"),
        @Index(name = "idx_sim_scenarios_status", columnList = "status")
})
public class SimulationScenario {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SimulationSession session;

    @Column(name = "scenario_name", nullable = false)
    private String scenarioName;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_type", nullable = false, length = 64)
    private ScenarioType scenarioType;

    @Column(name = "target_configuration", columnDefinition = "TEXT")
    private String targetConfiguration;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds = 300;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ScenarioStatus status = ScenarioStatus.ACTIVE;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    public SimulationScenario() {
        this.id = UUID.randomUUID().toString();
    }

    public SimulationScenario(SimulationSession session, String scenarioName, ScenarioType scenarioType, String targetConfiguration, int durationSeconds) {
        this();
        this.session = session;
        this.scenarioName = scenarioName;
        this.scenarioType = scenarioType;
        this.targetConfiguration = targetConfiguration;
        this.durationSeconds = durationSeconds > 0 ? durationSeconds : 300;
        this.status = ScenarioStatus.ACTIVE;
        this.startedAt = Instant.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SimulationSession getSession() {
        return session;
    }

    public void setSession(SimulationSession session) {
        this.session = session;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType;
    }

    public String getTargetConfiguration() {
        return targetConfiguration;
    }

    public void setTargetConfiguration(String targetConfiguration) {
        this.targetConfiguration = targetConfiguration;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public ScenarioStatus getStatus() {
        return status;
    }

    public void setStatus(ScenarioStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
