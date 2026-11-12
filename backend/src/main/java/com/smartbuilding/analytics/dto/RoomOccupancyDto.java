package com.smartbuilding.analytics.dto;

public class RoomOccupancyDto {
    private String roomId;
    private String roomNumber;
    private String roomName;
    private int occupancy;
    private int capacity;
    private double utilizationPercent;

    public RoomOccupancyDto() {}

    public RoomOccupancyDto(String roomId, String roomNumber, String roomName, int occupancy, int capacity, double utilizationPercent) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomName = roomName;
        this.occupancy = occupancy;
        this.capacity = capacity;
        this.utilizationPercent = utilizationPercent;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(double utilizationPercent) { this.utilizationPercent = utilizationPercent; }
}

