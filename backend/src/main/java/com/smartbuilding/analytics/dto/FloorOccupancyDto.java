package com.smartbuilding.analytics.dto;

public class FloorOccupancyDto {
    private String floorId;
    private int floorNumber;
    private String floorName;
    private int occupancy;
    private int capacity;
    private double utilizationPercent;

    public FloorOccupancyDto() {}

    public FloorOccupancyDto(String floorId, int floorNumber, String floorName, int occupancy, int capacity, double utilizationPercent) {
        this.floorId = floorId;
        this.floorNumber = floorNumber;
        this.floorName = floorName;
        this.occupancy = occupancy;
        this.capacity = capacity;
        this.utilizationPercent = utilizationPercent;
    }

    public String getFloorId() { return floorId; }
    public void setFloorId(String floorId) { this.floorId = floorId; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public String getFloorName() { return floorName; }
    public void setFloorName(String floorName) { this.floorName = floorName; }

    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(double utilizationPercent) { this.utilizationPercent = utilizationPercent; }
}

