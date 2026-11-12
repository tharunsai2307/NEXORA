package com.smartbuilding.analytics.service;

import com.smartbuilding.analytics.dto.*;
import com.smartbuilding.sensor.model.SensorType;

import java.time.Instant;

public interface AnalyticsAggregationService {

    AnalyticsOverviewResponse getOverview(String buildingId, Instant start, Instant end);

    SensorAnalyticsResponse getSensorAnalytics(String buildingId, String roomId, SensorType sensorType, Instant start, Instant end);

    OccupancyAnalyticsResponse getOccupancyAnalytics(String buildingId, Instant start, Instant end);

    AiAnalyticsResponse getAiAnalytics(String buildingId, Instant start, Instant end);

    IncidentAnalyticsResponse getIncidentAnalytics(String buildingId, Instant start, Instant end);

    EvacuationAnalyticsResponse getEvacuationAnalytics(String buildingId, Instant start, Instant end);

    HistoricalQueryResponse queryHistoricalData(String buildingId, String recordType, Instant start, Instant end, int page, int size);
}

