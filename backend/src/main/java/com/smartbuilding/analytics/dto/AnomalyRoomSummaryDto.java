package com.smartbuilding.analytics.dto;

import java.time.Instant;

public class AnomalyRoomSummaryDto {
    private String roomId;
    private String roomNumber;
    private String roomName;
    private long anomalyCount;
    private String highestSeverity;
    private Instant lastAnomalyAt;

    public AnomalyRoomSummaryDto() {}

    public AnomalyRoomSummaryDto(String roomId, String roomNumber, String roomName, long anomalyCount, String highestSeverity, Instant lastAnomalyAt) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomName = roomName;
        this.anomalyCount = anomalyCount;
        this.highestSeverity = highestSeverity;
        this.lastAnomalyAt = lastAnomalyAt;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public long getAnomalyCount() { return anomalyCount; }
    public void setAnomalyCount(long anomalyCount) { this.anomalyCount = anomalyCount; }

    public String getHighestSeverity() { return highestSeverity; }
    public void setHighestSeverity(String highestSeverity) { this.highestSeverity = highestSeverity; }

    public Instant getLastAnomalyAt() { return lastAnomalyAt; }
    public void setLastAnomalyAt(Instant lastAnomalyAt) { this.lastAnomalyAt = lastAnomalyAt; }
}

