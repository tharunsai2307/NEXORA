package com.smartbuilding.analytics.dto;

import java.util.ArrayList;
import java.util.List;

public class OccupancyAnalyticsResponse {

    private int currentTotalOccupancy;
    private double averageDailyOccupancy;
    private int peakOccupancy;
    private int peakHour;
    private double overallUtilizationPercent;
    private List<HourlyOccupancyDto> hourlyDistribution = new ArrayList<>();
    private List<FloorOccupancyDto> floorDistribution = new ArrayList<>();
    private List<RoomOccupancyDto> roomOccupancy = new ArrayList<>();
    private List<OccupancyComparisonPointDto> actualVsPredicted = new ArrayList<>();

    public OccupancyAnalyticsResponse() {}

    public int getCurrentTotalOccupancy() { return currentTotalOccupancy; }
    public void setCurrentTotalOccupancy(int currentTotalOccupancy) { this.currentTotalOccupancy = currentTotalOccupancy; }

    public double getAverageDailyOccupancy() { return averageDailyOccupancy; }
    public void setAverageDailyOccupancy(double averageDailyOccupancy) { this.averageDailyOccupancy = averageDailyOccupancy; }

    public int getPeakOccupancy() { return peakOccupancy; }
    public void setPeakOccupancy(int peakOccupancy) { this.peakOccupancy = peakOccupancy; }

    public int getPeakHour() { return peakHour; }
    public void setPeakHour(int peakHour) { this.peakHour = peakHour; }

    public double getOverallUtilizationPercent() { return overallUtilizationPercent; }
    public void setOverallUtilizationPercent(double overallUtilizationPercent) { this.overallUtilizationPercent = overallUtilizationPercent; }

    public List<HourlyOccupancyDto> getHourlyDistribution() { return hourlyDistribution; }
    public void setHourlyDistribution(List<HourlyOccupancyDto> hourlyDistribution) { this.hourlyDistribution = hourlyDistribution; }

    public List<FloorOccupancyDto> getFloorDistribution() { return floorDistribution; }
    public void setFloorDistribution(List<FloorOccupancyDto> floorDistribution) { this.floorDistribution = floorDistribution; }

    public List<RoomOccupancyDto> getRoomOccupancy() { return roomOccupancy; }
    public void setRoomOccupancy(List<RoomOccupancyDto> roomOccupancy) { this.roomOccupancy = roomOccupancy; }

    public List<OccupancyComparisonPointDto> getActualVsPredicted() { return actualVsPredicted; }
    public void setActualVsPredicted(List<OccupancyComparisonPointDto> actualVsPredicted) { this.actualVsPredicted = actualVsPredicted; }
}

