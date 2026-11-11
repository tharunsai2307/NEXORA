package com.smartbuilding.sensor.dto;

import com.smartbuilding.sensor.model.Sensor;
import com.smartbuilding.sensor.model.SensorStatus;
import com.smartbuilding.sensor.model.SensorType;
import com.smartbuilding.sensor.model.SensorUnit;

import java.time.Instant;

public class SensorResponse {

    private String id;
    private String roomId;
    private String roomName;
    private String roomCode;
    private String floorId;
    private String floorName;
    private Integer floorNumber;
    private String buildingId;
    private String buildingName;
    private String sensorCode;
    private String sensorName;
    private SensorType sensorType;
    private SensorUnit unit;
    private String unitSymbol;
    private SensorStatus status;
    private int simulationIntervalSeconds;
    private Double lastReadingValue;
    private Instant lastReadingAt;
    private Instant createdAt;

    public SensorResponse() {}

    public static SensorResponse fromEntity(Sensor sensor) {
        SensorResponse resp = new SensorResponse();
        resp.setId(sensor.getId());
        resp.setSensorCode(sensor.getSensorCode());
        resp.setSensorName(sensor.getSensorName());
        resp.setSensorType(sensor.getSensorType());
        resp.setUnit(sensor.getUnit());
        resp.setUnitSymbol(sensor.getUnit() != null ? sensor.getUnit().getSymbol() : "");
        resp.setStatus(sensor.getStatus());
        resp.setSimulationIntervalSeconds(sensor.getSimulationIntervalSeconds());
        resp.setLastReadingValue(sensor.getLastReadingValue());
        resp.setLastReadingAt(sensor.getLastReadingAt());
        resp.setCreatedAt(sensor.getCreatedAt());

        if (sensor.getRoom() != null) {
            resp.setRoomId(sensor.getRoom().getId());
            resp.setRoomName(sensor.getRoom().getRoomName());
            resp.setRoomCode(sensor.getRoom().getRoomCode());

            if (sensor.getRoom().getFloor() != null) {
                resp.setFloorId(sensor.getRoom().getFloor().getId());
                resp.setFloorName(sensor.getRoom().getFloor().getFloorName());
                resp.setFloorNumber(sensor.getRoom().getFloor().getFloorNumber());

                if (sensor.getRoom().getFloor().getBuilding() != null) {
                    resp.setBuildingId(sensor.getRoom().getFloor().getBuilding().getId());
                    resp.setBuildingName(sensor.getRoom().getFloor().getBuilding().getName());
                }
            }
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
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

    public SensorStatus getStatus() {
        return status;
    }

    public void setStatus(SensorStatus status) {
        this.status = status;
    }

    public int getSimulationIntervalSeconds() {
        return simulationIntervalSeconds;
    }

    public void setSimulationIntervalSeconds(int simulationIntervalSeconds) {
        this.simulationIntervalSeconds = simulationIntervalSeconds;
    }

    public Double getLastReadingValue() {
        return lastReadingValue;
    }

    public void setLastReadingValue(Double lastReadingValue) {
        this.lastReadingValue = lastReadingValue;
    }

    public Instant getLastReadingAt() {
        return lastReadingAt;
    }

    public void setLastReadingAt(Instant lastReadingAt) {
        this.lastReadingAt = lastReadingAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

