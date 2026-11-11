package com.smartbuilding.ai.model;

import com.smartbuilding.building.model.Room;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "anomaly_detections", indexes = {
    @Index(name = "idx_anomaly_detections_room_time", columnList = "room_id, detected_at DESC"),
    @Index(name = "idx_anomaly_detections_severity", columnList = "severity"),
    @Index(name = "idx_anomaly_detections_detected", columnList = "anomaly_detected")
})
public class AnomalyDetection {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "anomaly_score", nullable = false)
    private Double anomalyScore;

    @Column(name = "severity", length = 32, nullable = false)
    private String severity; // NORMAL, LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "anomaly_detected", nullable = false)
    private Boolean anomalyDetected = false;

    @Column(name = "contributing_features", columnDefinition = "TEXT")
    private String contributingFeatures;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "model_version", length = 64, nullable = false)
    private String modelVersion;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt = Instant.now();

    public AnomalyDetection() {}

    public AnomalyDetection(String id, Room room, Double anomalyScore, String severity,
                            Boolean anomalyDetected, String contributingFeatures,
                            String explanation, String modelVersion, Instant detectedAt) {
        this.id = id;
        this.room = room;
        this.anomalyScore = anomalyScore;
        this.severity = severity;
        this.anomalyDetected = anomalyDetected != null ? anomalyDetected : false;
        this.contributingFeatures = contributingFeatures;
        this.explanation = explanation;
        this.modelVersion = modelVersion;
        this.detectedAt = detectedAt != null ? detectedAt : Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Boolean getAnomalyDetected() { return anomalyDetected; }
    public void setAnomalyDetected(Boolean anomalyDetected) { this.anomalyDetected = anomalyDetected; }

    public String getContributingFeatures() { return contributingFeatures; }
    public void setContributingFeatures(String contributingFeatures) { this.contributingFeatures = contributingFeatures; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
