package com.smartbuilding.sensor.dto;

import com.smartbuilding.sensor.model.SensorType;
import com.smartbuilding.sensor.model.SensorUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateSensorRequest {

    private String sensorCode;

    @NotBlank(message = "Sensor name is required")
    private String sensorName;

    @NotNull(message = "Sensor type is required")
    private SensorType sensorType;

    private SensorUnit unit;

    private Integer simulationIntervalSeconds = 5;

    public CreateSensorRequest() {}

    public String getSensorCode() {
        return sensorCode;
    }

    public void setSensorCode(String sensorCode) {
        this.sensorCode = sensorCode;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public SensorUnit getUnit() {
        return unit;
    }

    public void setUnit(SensorUnit unit) {
        this.unit = unit;
    }

    public Integer getSimulationIntervalSeconds() {
        return simulationIntervalSeconds;
    }

    public void setSimulationIntervalSeconds(Integer simulationIntervalSeconds) {
        this.simulationIntervalSeconds = simulationIntervalSeconds;
    }
}
