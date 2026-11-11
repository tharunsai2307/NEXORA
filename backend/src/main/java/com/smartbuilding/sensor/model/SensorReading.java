package com.smartbuilding.sensor.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sensor_readings", indexes = {
        @Index(name = "idx_readings_sensor_time", columnList = "sensor_id, recorded_at DESC"),
        @Index(name = "idx_readings_recorded_at", columnList = "recorded_at DESC")
})
public class SensorReading {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "reading_value", nullable = false)
    private double readingValue;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_source", nullable = false, length = 32)
    private ReadingSource readingSource = ReadingSource.SIMULATED;

    @Column(name = "scenario_id", length = 36)
    private String scenarioId;

    public SensorReading() {
        this.id = UUID.randomUUID().toString();
    }

    public SensorReading(Sensor sensor, double readingValue, ReadingSource readingSource, String scenarioId) {
        this();
        this.sensor = sensor;
        this.readingValue = readingValue;
        this.readingSource = readingSource != null ? readingSource : ReadingSource.SIMULATED;
        this.scenarioId = scenarioId;
        this.recordedAt = Instant.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
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
