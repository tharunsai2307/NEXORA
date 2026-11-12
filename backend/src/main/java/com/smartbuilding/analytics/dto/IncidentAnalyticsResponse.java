package com.smartbuilding.analytics.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncidentAnalyticsResponse {

    private long totalIncidents;
    private long activeIncidents;
    private long resolvedIncidents;
    private double meanTimeToAcknowledgeMinutes;
    private double meanTimeToResolveMinutes;
    private Map<String, Long> severityDistribution = new HashMap<>();
    private Map<String, Long> categoryDistribution = new HashMap<>();
    private List<IncidentTrendPointDto> incidentsOverTime = new ArrayList<>();
    private List<IncidentHotspotDto> hotspotRooms = new ArrayList<>();

    public IncidentAnalyticsResponse() {}

    public long getTotalIncidents() { return totalIncidents; }
    public void setTotalIncidents(long totalIncidents) { this.totalIncidents = totalIncidents; }

    public long getActiveIncidents() { return activeIncidents; }
    public void setActiveIncidents(long activeIncidents) { this.activeIncidents = activeIncidents; }

    public long getResolvedIncidents() { return resolvedIncidents; }
    public void setResolvedIncidents(long resolvedIncidents) { this.resolvedIncidents = resolvedIncidents; }

    public double getMeanTimeToAcknowledgeMinutes() { return meanTimeToAcknowledgeMinutes; }
    public void setMeanTimeToAcknowledgeMinutes(double meanTimeToAcknowledgeMinutes) { this.meanTimeToAcknowledgeMinutes = meanTimeToAcknowledgeMinutes; }

    public double getMeanTimeToResolveMinutes() { return meanTimeToResolveMinutes; }
    public void setMeanTimeToResolveMinutes(double meanTimeToResolveMinutes) { this.meanTimeToResolveMinutes = meanTimeToResolveMinutes; }

    public Map<String, Long> getSeverityDistribution() { return severityDistribution; }
    public void setSeverityDistribution(Map<String, Long> severityDistribution) { this.severityDistribution = severityDistribution; }

    public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(Map<String, Long> categoryDistribution) { this.categoryDistribution = categoryDistribution; }

    public List<IncidentTrendPointDto> getIncidentsOverTime() { return incidentsOverTime; }
    public void setIncidentsOverTime(List<IncidentTrendPointDto> incidentsOverTime) { this.incidentsOverTime = incidentsOverTime; }

    public List<IncidentHotspotDto> getHotspotRooms() { return hotspotRooms; }
    public void setHotspotRooms(List<IncidentHotspotDto> hotspotRooms) { this.hotspotRooms = hotspotRooms; }
}

