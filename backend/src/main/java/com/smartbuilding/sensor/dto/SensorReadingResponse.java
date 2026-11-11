package com.smartbuilding.sensor.dto;

import com.smartbuilding.sensor.model.ReadingSource;
import com.smartbuilding.sensor.model.SensorReading;
import com.smartbuilding.sensor.model.SensorType;
import com.smartbuilding.sensor.model.SensorUnit;

import java.time.Instant;

public class SensorReadingResponse {

    private String id;
    private String sensorId;
    private String sensorCode;
    private String sensorName;
    private SensorType sensorType;
    private SensorUnit unit;
    private String unitSymbol;
    private double readingValue;
    private Instant recordedAt;
    private ReadingSource readingSource;
    private String scenarioId;

    public SensorReadingResponse() {}

    public static SensorReadingResponse fromEntity(SensorReading reading) {
        SensorReadingResponse resp = new SensorReadingResponse();
        resp.setId(reading.getId());
        resp.setReadingValue(reading.getReadingValue());
        resp.setRecordedAt(reading.getRecordedAt());
        resp.setReadingSource(reading.getReadingSource());
        resp.setScenarioId(reading.getScenarioId());

        if (reading.getSensor() != null) {
            resp.setSensorId(reading.getSensor().getId());
            resp.setSensorCode(reading.getSensor().getSensorCode());
            resp.setSensorName(reading.getSensor().getSensorName());
            resp.setSensorType(reading.getSensor().getSensorType());
            resp.setUnit(reading.getSensor().getUnit());
            resp.setUnitSymbol(reading.getSensor().getUnit() != null ? reading.getSensor().getUnit().getSymbol() : "");
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

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

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

    public String getUnitSymbol() {
        return unitSymbol;
    }

    public void setUnitSymbol(String unitSymbol) {
        this.unitSymbol = unitSymbol;
    }

    public double getReadingValue() {
        return readingValue;
    }

    public void setReadingValue(double readingValue) {
        this.readingValue = readingValue;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public ReadingSource getReadingSource() {
        return readingSource;
    }

    public void setReadingSource(ReadingSource readingSource) {
        this.readingSource = readingSource;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }
}

