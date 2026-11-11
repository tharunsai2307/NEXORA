package com.smartbuilding.simulation.dto;

import com.smartbuilding.simulation.model.ScenarioType;
import jakarta.validation.constraints.NotBlank;

public class StartSimulationRequest {

    @NotBlank(message = "Building ID is required")
    private String buildingId;

    private String sessionName;

    private ScenarioType scenarioType = ScenarioType.NORMAL;

    private Integer simulationIntervalSeconds = 5;

    public StartSimulationRequest() {}

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType;
    }

    public Integer getSimulationIntervalSeconds() {
        return simulationIntervalSeconds;
    }

    public void setSimulationIntervalSeconds(Integer simulationIntervalSeconds) {
        this.simulationIntervalSeconds = simulationIntervalSeconds;
    }
}

