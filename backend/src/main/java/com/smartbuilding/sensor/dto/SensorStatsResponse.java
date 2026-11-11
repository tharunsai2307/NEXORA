package com.smartbuilding.sensor.dto;

import java.util.Map;

public class SensorStatsResponse {

    private long totalSensors;
    private long activeSensors;
    private long inactiveSensors;
    private long errorSensors;
    private boolean simulationRunning;
    private String activeSessionId;
    private Map<String, Long> distributionByType;

    public SensorStatsResponse() {}

    public SensorStatsResponse(long totalSensors, long activeSensors, long inactiveSensors, long errorSensors,
                               boolean simulationRunning, String activeSessionId, Map<String, Long> distributionByType) {
        this.totalSensors = totalSensors;
        this.activeSensors = activeSensors;
        this.inactiveSensors = inactiveSensors;
        this.errorSensors = errorSensors;
        this.simulationRunning = simulationRunning;
        this.activeSessionId = activeSessionId;
        this.distributionByType = distributionByType;
    }

    public long getTotalSensors() {
        return totalSensors;
    }

    public void setTotalSensors(long totalSensors) {
        this.totalSensors = totalSensors;
    }

    public long getActiveSensors() {
        return activeSensors;
    }

    public void setActiveSensors(long activeSensors) {
        this.activeSensors = activeSensors;
    }

    public long getInactiveSensors() {
        return inactiveSensors;
    }

    public void setInactiveSensors(long inactiveSensors) {
        this.inactiveSensors = inactiveSensors;
    }

    public long getErrorSensors() {
        return errorSensors;
    }

    public void setErrorSensors(long errorSensors) {
        this.errorSensors = errorSensors;
    }

    public boolean isSimulationRunning() {
        return simulationRunning;
    }

    public void setSimulationRunning(boolean simulationRunning) {
        this.simulationRunning = simulationRunning;
    }

    public String getActiveSessionId() {
        return activeSessionId;
    }

    public void setActiveSessionId(String activeSessionId) {
        this.activeSessionId = activeSessionId;
    }

    public Map<String, Long> getDistributionByType() {
        return distributionByType;
    }

    public void setDistributionByType(Map<String, Long> distributionByType) {
        this.distributionByType = distributionByType;
    }
}
