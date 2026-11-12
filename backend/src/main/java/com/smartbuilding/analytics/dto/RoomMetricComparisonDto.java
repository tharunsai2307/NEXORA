package com.smartbuilding.analytics.dto;

public class RoomMetricComparisonDto {
    private String roomId;
    private String roomNumber;
    private String roomName;
    private double avgValue;
    private double peakValue;
    private long violationCount;

    public RoomMetricComparisonDto() {}

    public RoomMetricComparisonDto(String roomId, String roomNumber, String roomName, double avgValue, double peakValue, long violationCount) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomName = roomName;
        this.avgValue = avgValue;
        this.peakValue = peakValue;
        this.violationCount = violationCount;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public double getAvgValue() { return avgValue; }
    public void setAvgValue(double avgValue) { this.avgValue = avgValue; }

    public double getPeakValue() { return peakValue; }
    public void setPeakValue(double peakValue) { this.peakValue = peakValue; }

    public long getViolationCount() { return violationCount; }
    public void setViolationCount(long violationCount) { this.violationCount = violationCount; }
}

