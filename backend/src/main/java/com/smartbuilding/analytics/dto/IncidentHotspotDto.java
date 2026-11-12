package com.smartbuilding.analytics.dto;

public class IncidentHotspotDto {
    private String roomId;
    private String roomNumber;
    private String roomName;
    private long incidentCount;
    private String mostFrequentCategory;

    public IncidentHotspotDto() {}

    public IncidentHotspotDto(String roomId, String roomNumber, String roomName, long incidentCount, String mostFrequentCategory) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomName = roomName;
        this.incidentCount = incidentCount;
        this.mostFrequentCategory = mostFrequentCategory;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public long getIncidentCount() { return incidentCount; }
    public void setIncidentCount(long incidentCount) { this.incidentCount = incidentCount; }

    public String getMostFrequentCategory() { return mostFrequentCategory; }
    public void setMostFrequentCategory(String mostFrequentCategory) { this.mostFrequentCategory = mostFrequentCategory; }
}

