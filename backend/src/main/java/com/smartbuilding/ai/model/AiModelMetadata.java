package com.smartbuilding.ai.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_model_metadata", indexes = {
    @Index(name = "idx_ai_model_name", columnList = "model_name")
})
public class AiModelMetadata {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "model_name", length = 128, nullable = false)
    private String modelName;

    @Column(name = "model_version", length = 64, nullable = false)
    private String modelVersion;

    @Column(name = "model_type", length = 64, nullable = false)
    private String modelType;

    @Column(name = "training_dataset_size", nullable = false)
    private Integer trainingDatasetSize = 0;

    @Column(name = "training_timestamp")
    private Instant trainingTimestamp;

    @Column(name = "performance_metrics", columnDefinition = "TEXT")
    private String performanceMetrics;

    @Column(name = "model_status", length = 32, nullable = false)
    private String modelStatus = "READY";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AiModelMetadata() {}

    public AiModelMetadata(String id, String modelName, String modelVersion, String modelType,
                           Integer trainingDatasetSize, Instant trainingTimestamp,
                           String performanceMetrics, String modelStatus) {
        this.id = id;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.modelType = modelType;
        this.trainingDatasetSize = trainingDatasetSize != null ? trainingDatasetSize : 0;
        this.trainingTimestamp = trainingTimestamp;
        this.performanceMetrics = performanceMetrics;
        this.modelStatus = modelStatus != null ? modelStatus : "READY";
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }

    public Integer getTrainingDatasetSize() { return trainingDatasetSize; }
    public void setTrainingDatasetSize(Integer trainingDatasetSize) { this.trainingDatasetSize = trainingDatasetSize; }

    public Instant getTrainingTimestamp() { return trainingTimestamp; }
    public void setTrainingTimestamp(Instant trainingTimestamp) { this.trainingTimestamp = trainingTimestamp; }

    public String getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(String performanceMetrics) { this.performanceMetrics = performanceMetrics; }

    public String getModelStatus() { return modelStatus; }
    public void setModelStatus(String modelStatus) { this.modelStatus = modelStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
