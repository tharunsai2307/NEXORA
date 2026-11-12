package com.smartbuilding.analytics.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiAnalyticsResponse {

    private long totalAnomalies;
    private long criticalRiskRooms;
    private long highRiskRooms;
    private long normalRooms;
    private double avgAnomalyScore;
    private Map<String, Long> riskLevelDistribution = new HashMap<>();
    private List<AnomalyRoomSummaryDto> topAnomalyRooms = new ArrayList<>();
    private List<AnomalyTrendPointDto> anomaliesOverTime = new ArrayList<>();
    private List<AiModelSummaryDto> modelMetadata = new ArrayList<>();

    public AiAnalyticsResponse() {}

    public long getTotalAnomalies() { return totalAnomalies; }
    public void setTotalAnomalies(long totalAnomalies) { this.totalAnomalies = totalAnomalies; }

    public long getCriticalRiskRooms() { return criticalRiskRooms; }
    public void setCriticalRiskRooms(long criticalRiskRooms) { this.criticalRiskRooms = criticalRiskRooms; }

    public long getHighRiskRooms() { return highRiskRooms; }
    public void setHighRiskRooms(long highRiskRooms) { this.highRiskRooms = highRiskRooms; }

    public long getNormalRooms() { return normalRooms; }
    public void setNormalRooms(long normalRooms) { this.normalRooms = normalRooms; }

    public double getAvgAnomalyScore() { return avgAnomalyScore; }
    public void setAvgAnomalyScore(double avgAnomalyScore) { this.avgAnomalyScore = avgAnomalyScore; }

    public Map<String, Long> getRiskLevelDistribution() { return riskLevelDistribution; }
    public void setRiskLevelDistribution(Map<String, Long> riskLevelDistribution) { this.riskLevelDistribution = riskLevelDistribution; }

    public List<AnomalyRoomSummaryDto> getTopAnomalyRooms() { return topAnomalyRooms; }
    public void setTopAnomalyRooms(List<AnomalyRoomSummaryDto> topAnomalyRooms) { this.topAnomalyRooms = topAnomalyRooms; }

    public List<AnomalyTrendPointDto> getAnomaliesOverTime() { return anomaliesOverTime; }
    public void setAnomaliesOverTime(List<AnomalyTrendPointDto> anomaliesOverTime) { this.anomaliesOverTime = anomaliesOverTime; }

    public List<AiModelSummaryDto> getModelMetadata() { return modelMetadata; }
    public void setModelMetadata(List<AiModelSummaryDto> modelMetadata) { this.modelMetadata = modelMetadata; }
}

