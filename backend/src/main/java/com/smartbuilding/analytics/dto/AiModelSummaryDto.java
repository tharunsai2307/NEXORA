package com.smartbuilding.analytics.dto;

public class AiModelSummaryDto {
    private String modelType;
    private String version;
    private String status;
    private Double accuracy;

    public AiModelSummaryDto() {}

    public AiModelSummaryDto(String modelType, String version, String status, Double accuracy) {
        this.modelType = modelType;
        this.version = version;
        this.status = status;
        this.accuracy = accuracy;
    }

    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
}

