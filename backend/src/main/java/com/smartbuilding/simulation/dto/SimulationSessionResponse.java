package com.smartbuilding.simulation.dto;

import com.smartbuilding.simulation.model.ScenarioType;
import com.smartbuilding.simulation.model.SessionStatus;
import com.smartbuilding.simulation.model.SimulationSession;

import java.time.Instant;

public class SimulationSessionResponse {

    private String id;
    private String buildingId;
    private String buildingName;
    private String buildingCode;
    private String sessionName;
    private String startedBy;
    private SessionStatus status;
    private ScenarioType scenarioType;
    private int simulationIntervalSeconds;
    private int activeSensorCount;
    private Instant startedAt;
    private Instant pausedAt;
    private Instant stoppedAt;

    public SimulationSessionResponse() {}

    public static SimulationSessionResponse fromEntity(SimulationSession session, int activeSensorCount) {
        SimulationSessionResponse resp = new SimulationSessionResponse();
        resp.setId(session.getId());
        resp.setSessionName(session.getSessionName());
        resp.setStartedBy(session.getStartedBy());
        resp.setStatus(session.getStatus());
        resp.setScenarioType(session.getScenarioType());
        resp.setSimulationIntervalSeconds(session.getSimulationIntervalSeconds());
        resp.setActiveSensorCount(activeSensorCount);
        resp.setStartedAt(session.getStartedAt());
        resp.setPausedAt(session.getPausedAt());
        resp.setStoppedAt(session.getStoppedAt());

        if (session.getBuilding() != null) {
            resp.setBuildingId(session.getBuilding().getId());
            resp.setBuildingName(session.getBuilding().getName());
            resp.setBuildingCode(session.getBuilding().getBuildingCode());
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

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
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

    public int getActiveSensorCount() {
        return activeSensorCount;
    }

    public void setActiveSensorCount(int activeSensorCount) {
        this.activeSensorCount = activeSensorCount;
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

