package com.smartbuilding.sensor.dto;

import com.smartbuilding.sensor.model.SensorStatus;

public class UpdateSensorRequest {

    private String sensorName;
    private Integer simulationIntervalSeconds;
    private SensorStatus status;

    public UpdateSensorRequest() {}

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public Integer getSimulationIntervalSeconds() {
        return simulationIntervalSeconds;
    }

    public void setSimulationIntervalSeconds(Integer simulationIntervalSeconds) {
        this.simulationIntervalSeconds = simulationIntervalSeconds;
    }

    public SensorStatus getStatus() {
        return status;
    }

    public void setStatus(SensorStatus status) {
        this.status = status;
    }
}

