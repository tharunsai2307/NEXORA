package com.smartbuilding.sensor.model;

import com.smartbuilding.building.model.Room;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sensors", uniqueConstraints = {
        @UniqueConstraint(name = "uq_room_sensor_type", columnNames = {"room_id", "sensor_type"})
})
public class Sensor {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "sensor_code", nullable = false, unique = true, length = 64)
    private String sensorCode;

    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 32)
    private SensorType sensorType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SensorUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SensorStatus status = SensorStatus.ACTIVE;

    @Column(name = "simulation_interval_seconds", nullable = false)
    private int simulationIntervalSeconds = 5;

    @Column(name = "last_reading_value")
    private Double lastReadingValue;

    @Column(name = "last_reading_at")
    private Instant lastReadingAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Sensor() {
        this.id = UUID.randomUUID().toString();
    }

    public Sensor(Room room, String sensorCode, String sensorName, SensorType sensorType, SensorUnit unit) {
        this();
        this.room = room;
        this.sensorCode = sensorCode;
        this.sensorName = sensorName;
        this.sensorType = sensorType;
        this.unit = unit;
        this.status = SensorStatus.ACTIVE;
        this.simulationIntervalSeconds = 5;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
