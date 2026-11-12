package com.smartbuilding.analytics.dto;

public class HourlyOccupancyDto {
    private int hour;
    private double avgOccupancy;
    private int peakOccupancy;

    public HourlyOccupancyDto() {}

    public HourlyOccupancyDto(int hour, double avgOccupancy, int peakOccupancy) {
        this.hour = hour;
        this.avgOccupancy = avgOccupancy;
        this.peakOccupancy = peakOccupancy;
    }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public double getAvgOccupancy() { return avgOccupancy; }
    public void setAvgOccupancy(double avgOccupancy) { this.avgOccupancy = avgOccupancy; }

    public int getPeakOccupancy() { return peakOccupancy; }
    public void setPeakOccupancy(int peakOccupancy) { this.peakOccupancy = peakOccupancy; }
}

