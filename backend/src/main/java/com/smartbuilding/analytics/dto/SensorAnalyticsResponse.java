package com.smartbuilding.analytics.dto;

import java.util.ArrayList;
import java.util.List;

public class SensorAnalyticsResponse {

    private String sensorType;
    private long totalReadings;
    private double avgValue;
    private double minValue;
    private double maxValue;
    private long thresholdViolations;
    private List<SensorTimeSeriesPointDto> timeSeries = new ArrayList<>();
    private List<RoomMetricComparisonDto> roomComparisons = new ArrayList<>();

    public SensorAnalyticsResponse() {}

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public long getTotalReadings() { return totalReadings; }
    public void setTotalReadings(long totalReadings) { this.totalReadings = totalReadings; }

    public double getAvgValue() { return avgValue; }
    public void setAvgValue(double avgValue) { this.avgValue = avgValue; }

    public double getMinValue() { return minValue; }
    public void setMinValue(double minValue) { this.minValue = minValue; }

    public double getMaxValue() { return maxValue; }
    public void setMaxValue(double maxValue) { this.maxValue = maxValue; }

    public long getThresholdViolations() { return thresholdViolations; }
    public void setThresholdViolations(long thresholdViolations) { this.thresholdViolations = thresholdViolations; }

    public List<SensorTimeSeriesPointDto> getTimeSeries() { return timeSeries; }
    public void setTimeSeries(List<SensorTimeSeriesPointDto> timeSeries) { this.timeSeries = timeSeries; }

    public List<RoomMetricComparisonDto> getRoomComparisons() { return roomComparisons; }
    public void setRoomComparisons(List<RoomMetricComparisonDto> roomComparisons) { this.roomComparisons = roomComparisons; }
}

