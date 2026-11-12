package com.smartbuilding.analytics.controller;

import com.smartbuilding.analytics.dto.*;
import com.smartbuilding.analytics.service.AnalyticsAggregationService;
import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.sensor.model.SensorType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsAggregationService analyticsService;

    public AnalyticsController(AnalyticsAggregationService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AnalyticsOverviewResponse>> getOverview(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        AnalyticsOverviewResponse data = analyticsService.getOverview(buildingId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Overview analytics retrieved", data));
    }

    @GetMapping("/sensors")
    public ResponseEntity<ApiResponse<SensorAnalyticsResponse>> getSensorAnalytics(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) SensorType sensorType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        SensorAnalyticsResponse data = analyticsService.getSensorAnalytics(buildingId, roomId, sensorType, start, end);
        return ResponseEntity.ok(ApiResponse.success("Sensor analytics retrieved", data));
    }

    @GetMapping("/occupancy")
    public ResponseEntity<ApiResponse<OccupancyAnalyticsResponse>> getOccupancyAnalytics(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        OccupancyAnalyticsResponse data = analyticsService.getOccupancyAnalytics(buildingId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Occupancy analytics retrieved", data));
    }

    @GetMapping("/ai")
    public ResponseEntity<ApiResponse<AiAnalyticsResponse>> getAiAnalytics(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        AiAnalyticsResponse data = analyticsService.getAiAnalytics(buildingId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Edge AI diagnostics retrieved", data));
    }

    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse<IncidentAnalyticsResponse>> getIncidentAnalytics(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        IncidentAnalyticsResponse data = analyticsService.getIncidentAnalytics(buildingId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Incident analytics retrieved", data));
    }

    @GetMapping("/evacuations")
    public ResponseEntity<ApiResponse<EvacuationAnalyticsResponse>> getEvacuationAnalytics(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        EvacuationAnalyticsResponse data = analyticsService.getEvacuationAnalytics(buildingId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Evacuation analytics retrieved", data));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<HistoricalQueryResponse>> queryHistoricalData(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        HistoricalQueryResponse data = analyticsService.queryHistoricalData(buildingId, recordType, start, end, page, size);
        return ResponseEntity.ok(ApiResponse.success("Historical data retrieved", data));
    }
}

