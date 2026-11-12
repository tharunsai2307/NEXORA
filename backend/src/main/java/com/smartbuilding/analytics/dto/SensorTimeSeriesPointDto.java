package com.smartbuilding.analytics.dto;

public class SensorTimeSeriesPointDto {
    private String timestamp;
    private double avgValue;
    private double minValue;
    private double maxValue;
    private long count;

    public SensorTimeSeriesPointDto() {}

    public SensorTimeSeriesPointDto(String timestamp, double avgValue, double minValue, double maxValue, long count) {
        this.timestamp = timestamp;
        this.avgValue = avgValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.count = count;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public double getAvgValue() { return avgValue; }
    public void setAvgValue(double avgValue) { this.avgValue = avgValue; }

    public double getMinValue() { return minValue; }
    public void setMinValue(double minValue) { this.minValue = minValue; }

    public double getMaxValue() { return maxValue; }
    public void setMaxValue(double maxValue) { this.maxValue = maxValue; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}

