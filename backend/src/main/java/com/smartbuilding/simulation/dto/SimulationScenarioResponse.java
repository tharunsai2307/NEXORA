package com.smartbuilding.simulation.dto;

import com.smartbuilding.simulation.model.ScenarioStatus;
import com.smartbuilding.simulation.model.ScenarioType;
import com.smartbuilding.simulation.model.SimulationScenario;

import java.time.Instant;

public class SimulationScenarioResponse {

    private String id;
    private String sessionId;
    private String scenarioName;
    private ScenarioType scenarioType;
    private String targetConfiguration;
    private int durationSeconds;
    private ScenarioStatus status;
    private Instant startedAt;
    private Instant endedAt;

    public SimulationScenarioResponse() {}

    public static SimulationScenarioResponse fromEntity(SimulationScenario scenario) {
        SimulationScenarioResponse resp = new SimulationScenarioResponse();
        resp.setId(scenario.getId());
        resp.setScenarioName(scenario.getScenarioName());
        resp.setScenarioType(scenario.getScenarioType());
        resp.setTargetConfiguration(scenario.getTargetConfiguration());
        resp.setDurationSeconds(scenario.getDurationSeconds());
        resp.setStatus(scenario.getStatus());
        resp.setStartedAt(scenario.getStartedAt());
        resp.setEndedAt(scenario.getEndedAt());

        if (scenario.getSession() != null) {
            resp.setSessionId(scenario.getSession().getId());
        }

        return resp;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
