package com.smartbuilding.analytics.dto;

public class OccupancyComparisonPointDto {
    private String timestamp;
    private String roomId;
    private String roomNumber;
    private int actualOccupancy;
    private int predictedOccupancy;
    private double variance;

    public OccupancyComparisonPointDto() {}

    public OccupancyComparisonPointDto(String timestamp, String roomId, String roomNumber, int actualOccupancy, int predictedOccupancy, double variance) {
        this.timestamp = timestamp;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.actualOccupancy = actualOccupancy;
        this.predictedOccupancy = predictedOccupancy;
        this.variance = variance;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getActualOccupancy() { return actualOccupancy; }
    public void setActualOccupancy(int actualOccupancy) { this.actualOccupancy = actualOccupancy; }

    public int getPredictedOccupancy() { return predictedOccupancy; }
    public void setPredictedOccupancy(int predictedOccupancy) { this.predictedOccupancy = predictedOccupancy; }

    public double getVariance() { return variance; }
    public void setVariance(double variance) { this.variance = variance; }
}

