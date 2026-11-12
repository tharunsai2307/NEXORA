package com.smartbuilding.analytics.dto;

public class IncidentTrendPointDto {
    private String timeLabel;
    private long count;
    private long criticalCount;

    public IncidentTrendPointDto() {}

    public IncidentTrendPointDto(String timeLabel, long count, long criticalCount) {
        this.timeLabel = timeLabel;
        this.count = count;
        this.criticalCount = criticalCount;
    }

    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public long getCriticalCount() { return criticalCount; }
    public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }
}

