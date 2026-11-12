package com.smartbuilding.analytics.dto;

import java.time.Instant;

public class HistoricalDataPointDto {
    private String id;
    private Instant timestamp;
    private String recordType;
    private String buildingId;
    private String buildingName;
    private Integer floorNumber;
    private String roomId;
    private String roomNumber;
    private String metricName;
    private String metricValue;
    private String severityOrStatus;
    private String details;

    public HistoricalDataPointDto() {}

    public HistoricalDataPointDto(String id, Instant timestamp, String recordType, String buildingId, String buildingName,
                                  Integer floorNumber, String roomId, String roomNumber, String metricName,
                                  String metricValue, String severityOrStatus, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.recordType = recordType;
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.floorNumber = floorNumber;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.severityOrStatus = severityOrStatus;
        this.details = details;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public String getMetricValue() { return metricValue; }
    public void setMetricValue(String metricValue) { this.metricValue = metricValue; }

    public String getSeverityOrStatus() { return severityOrStatus; }
    public void setSeverityOrStatus(String severityOrStatus) { this.severityOrStatus = severityOrStatus; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

