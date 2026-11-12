package com.smartbuilding.analytics.dto;

import java.util.ArrayList;
import java.util.List;

public class EvacuationAnalyticsResponse {

    private long totalSessions;
    private long completedSessions;
    private long activeSessions;
    private double averageDurationSeconds;
    private int totalPersonsEvacuated;
    private int assistanceRequestsHandled;
    private int blockedConnectionsEncountered;
    private List<EvacuationClearancePointDto> clearanceTimeline = new ArrayList<>();

    public EvacuationAnalyticsResponse() {}

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

    public long getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(long completedSessions) { this.completedSessions = completedSessions; }

    public long getActiveSessions() { return activeSessions; }
    public void setActiveSessions(long activeSessions) { this.activeSessions = activeSessions; }

    public double getAverageDurationSeconds() { return averageDurationSeconds; }
    public void setAverageDurationSeconds(double averageDurationSeconds) { this.averageDurationSeconds = averageDurationSeconds; }

    public int getTotalPersonsEvacuated() { return totalPersonsEvacuated; }
    public void setTotalPersonsEvacuated(int totalPersonsEvacuated) { this.totalPersonsEvacuated = totalPersonsEvacuated; }

    public int getAssistanceRequestsHandled() { return assistanceRequestsHandled; }
    public void setAssistanceRequestsHandled(int assistanceRequestsHandled) { this.assistanceRequestsHandled = assistanceRequestsHandled; }

    public int getBlockedConnectionsEncountered() { return blockedConnectionsEncountered; }
    public void setBlockedConnectionsEncountered(int blockedConnectionsEncountered) { this.blockedConnectionsEncountered = blockedConnectionsEncountered; }

    public List<EvacuationClearancePointDto> getClearanceTimeline() { return clearanceTimeline; }
    public void setClearanceTimeline(List<EvacuationClearancePointDto> clearanceTimeline) { this.clearanceTimeline = clearanceTimeline; }
}

