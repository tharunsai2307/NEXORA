package com.smartbuilding.ai.model;

import com.smartbuilding.building.model.Room;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "risk_assessments", indexes = {
    @Index(name = "idx_risk_assessments_room_time", columnList = "room_id, assessed_at DESC"),
    @Index(name = "idx_risk_assessments_level", columnList = "risk_level")
})
public class RiskAssessment {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "risk_level", length = 32, nullable = false)
    private String riskLevel; // NORMAL, WARNING, HIGH_RISK, CRITICAL

    @Column(name = "temperature_score", nullable = false)
    private Double temperatureScore = 0.0;

    @Column(name = "smoke_score", nullable = false)
    private Double smokeScore = 0.0;

    @Column(name = "air_quality_score", nullable = false)
    private Double airQualityScore = 0.0;

    @Column(name = "occupancy_score", nullable = false)
    private Double occupancyScore = 0.0;

    @Column(name = "anomaly_score", nullable = false)
    private Double anomalyScore = 0.0;

    @Column(name = "contributing_factors", columnDefinition = "TEXT")
    private String contributingFactors;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt = Instant.now();

    public RiskAssessment() {}

    public RiskAssessment(String id, Room room, Double riskScore, String riskLevel,
                          Double temperatureScore, Double smokeScore, Double airQualityScore,
                          Double occupancyScore, Double anomalyScore, String contributingFactors,
                          String explanation, Instant assessedAt) {
        this.id = id;
        this.room = room;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.temperatureScore = temperatureScore != null ? temperatureScore : 0.0;
        this.smokeScore = smokeScore != null ? smokeScore : 0.0;
        this.airQualityScore = airQualityScore != null ? airQualityScore : 0.0;
        this.occupancyScore = occupancyScore != null ? occupancyScore : 0.0;
        this.anomalyScore = anomalyScore != null ? anomalyScore : 0.0;
        this.contributingFactors = contributingFactors;
        this.explanation = explanation;
        this.assessedAt = assessedAt != null ? assessedAt : Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Double getTemperatureScore() { return temperatureScore; }
    public void setTemperatureScore(Double temperatureScore) { this.temperatureScore = temperatureScore; }

    public Double getSmokeScore() { return smokeScore; }
    public void setSmokeScore(Double smokeScore) { this.smokeScore = smokeScore; }

    public Double getAirQualityScore() { return airQualityScore; }
    public void setAirQualityScore(Double airQualityScore) { this.airQualityScore = airQualityScore; }

    public Double getOccupancyScore() { return occupancyScore; }
    public void setOccupancyScore(Double occupancyScore) { this.occupancyScore = occupancyScore; }

    public Double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }

    public String getContributingFactors() { return contributingFactors; }
    public void setContributingFactors(String contributingFactors) { this.contributingFactors = contributingFactors; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Instant getAssessedAt() { return assessedAt; }
    public void setAssessedAt(Instant assessedAt) { this.assessedAt = assessedAt; }
}

