package com.smartbuilding.simulation.dto;

import com.smartbuilding.simulation.model.ScenarioType;
import jakarta.validation.constraints.NotNull;

public class ScenarioRequest {

    @NotNull(message = "Scenario type is required")
    private ScenarioType scenarioType;

    private String scenarioName;

    private String targetConfiguration; // JSON configuration (e.g. target room IDs, thermal ramp values)

    private Integer durationSeconds = 300;

    public ScenarioRequest() {}

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public String getTargetConfiguration() {
        return targetConfiguration;
    }

    public void setTargetConfiguration(String targetConfiguration) {
        this.targetConfiguration = targetConfiguration;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}

