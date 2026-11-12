package com.smartbuilding.analytics.dto;

import java.util.List;

public class AnalyticsOverviewResponse {

    private long totalReadings;
    private long totalIncidents;
    private long criticalIncidents;
    private double averageOccupancy;
    private int peakOccupancy;
    private double averageTemperature;
    private double averageSmokeLevel;
    private double averageAirQuality;
    private double averageHumidity;
    private long totalAnomaliesDetected;
    private long highRiskRoomsCount;
    private long totalEvacuationSessions;
    private double averageEvacuationDurationSeconds;
    private double systemSafetyScore;

    public AnalyticsOverviewResponse() {}

    public long getTotalReadings() { return totalReadings; }
    public void setTotalReadings(long totalReadings) { this.totalReadings = totalReadings; }

    public long getTotalIncidents() { return totalIncidents; }
    public void setTotalIncidents(long totalIncidents) { this.totalIncidents = totalIncidents; }

    public long getCriticalIncidents() { return criticalIncidents; }
    public void setCriticalIncidents(long criticalIncidents) { this.criticalIncidents = criticalIncidents; }

    public double getAverageOccupancy() { return averageOccupancy; }
    public void setAverageOccupancy(double averageOccupancy) { this.averageOccupancy = averageOccupancy; }

    public int getPeakOccupancy() { return peakOccupancy; }
    public void setPeakOccupancy(int peakOccupancy) { this.peakOccupancy = peakOccupancy; }

    public double getAverageTemperature() { return averageTemperature; }
    public void setAverageTemperature(double averageTemperature) { this.averageTemperature = averageTemperature; }

    public double getAverageSmokeLevel() { return averageSmokeLevel; }
    public void setAverageSmokeLevel(double averageSmokeLevel) { this.averageSmokeLevel = averageSmokeLevel; }

    public double getAverageAirQuality() { return averageAirQuality; }
    public void setAverageAirQuality(double averageAirQuality) { this.averageAirQuality = averageAirQuality; }

    public double getAverageHumidity() { return averageHumidity; }
    public void setAverageHumidity(double averageHumidity) { this.averageHumidity = averageHumidity; }

    public long getTotalAnomaliesDetected() { return totalAnomaliesDetected; }
    public void setTotalAnomaliesDetected(long totalAnomaliesDetected) { this.totalAnomaliesDetected = totalAnomaliesDetected; }

    public long getHighRiskRoomsCount() { return highRiskRoomsCount; }
    public void setHighRiskRoomsCount(long highRiskRoomsCount) { this.highRiskRoomsCount = highRiskRoomsCount; }

    public long getTotalEvacuationSessions() { return totalEvacuationSessions; }
    public void setTotalEvacuationSessions(long totalEvacuationSessions) { this.totalEvacuationSessions = totalEvacuationSessions; }

    public double getAverageEvacuationDurationSeconds() { return averageEvacuationDurationSeconds; }
    public void setAverageEvacuationDurationSeconds(double averageEvacuationDurationSeconds) { this.averageEvacuationDurationSeconds = averageEvacuationDurationSeconds; }

    public double getSystemSafetyScore() { return systemSafetyScore; }
    public void setSystemSafetyScore(double systemSafetyScore) { this.systemSafetyScore = systemSafetyScore; }
}

