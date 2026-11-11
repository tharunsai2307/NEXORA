package com.smartbuilding.ai.model;

import com.smartbuilding.building.model.Room;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_predictions", indexes = {
    @Index(name = "idx_ai_predictions_room_time", columnList = "room_id, prediction_timestamp DESC"),
    @Index(name = "idx_ai_predictions_type", columnList = "prediction_type")
})
public class AiPrediction {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "prediction_type", length = 64, nullable = false)
    private String predictionType;

    @Column(name = "predicted_value", nullable = false)
    private Double predictedValue;

    @Column(name = "prediction_horizon_minutes", nullable = false)
    private Integer predictionHorizonMinutes = 15;

    @Column(name = "confidence_or_score")
    private Double confidenceOrScore;

    @Column(name = "model_version", length = 64, nullable = false)
    private String modelVersion;

    @Column(name = "prediction_timestamp", nullable = false)
    private Instant predictionTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AiPrediction() {}

    public AiPrediction(String id, Room room, String predictionType, Double predictedValue,
                        Integer predictionHorizonMinutes, Double confidenceOrScore,
                        String modelVersion, Instant predictionTimestamp) {
        this.id = id;
        this.room = room;
        this.predictionType = predictionType;
        this.predictedValue = predictedValue;
        this.predictionHorizonMinutes = predictionHorizonMinutes != null ? predictionHorizonMinutes : 15;
        this.confidenceOrScore = confidenceOrScore;
        this.modelVersion = modelVersion;
        this.predictionTimestamp = predictionTimestamp;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getPredictionType() { return predictionType; }
    public void setPredictionType(String predictionType) { this.predictionType = predictionType; }

    public Double getPredictedValue() { return predictedValue; }
    public void setPredictedValue(Double predictedValue) { this.predictedValue = predictedValue; }

    public Integer getPredictionHorizonMinutes() { return predictionHorizonMinutes; }
    public void setPredictionHorizonMinutes(Integer predictionHorizonMinutes) { this.predictionHorizonMinutes = predictionHorizonMinutes; }

    public Double getConfidenceOrScore() { return confidenceOrScore; }
    public void setConfidenceOrScore(Double confidenceOrScore) { this.confidenceOrScore = confidenceOrScore; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Instant getPredictionTimestamp() { return predictionTimestamp; }
    public void setPredictionTimestamp(Instant predictionTimestamp) { this.predictionTimestamp = predictionTimestamp; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
