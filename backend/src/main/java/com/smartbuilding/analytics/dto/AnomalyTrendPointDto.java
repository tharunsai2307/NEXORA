package com.smartbuilding.analytics.dto;

public class AnomalyTrendPointDto {
    private String timeLabel;
    private long anomalyCount;
    private double avgScore;

    public AnomalyTrendPointDto() {}

    public AnomalyTrendPointDto(String timeLabel, long anomalyCount, double avgScore) {
        this.timeLabel = timeLabel;
        this.anomalyCount = anomalyCount;
        this.avgScore = avgScore;
    }

    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }

    public long getAnomalyCount() { return anomalyCount; }
    public void setAnomalyCount(long anomalyCount) { this.anomalyCount = anomalyCount; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
}

