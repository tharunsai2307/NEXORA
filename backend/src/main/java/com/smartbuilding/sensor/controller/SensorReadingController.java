package com.smartbuilding.sensor.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.sensor.dto.SensorReadingResponse;
import com.smartbuilding.sensor.service.SensorReadingService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SensorReadingController {

    private final SensorReadingService readingService;

    public SensorReadingController(SensorReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping("/sensors/{sensorId}/readings")
    public ResponseEntity<ApiResponse<Page<SensorReadingResponse>>> getSensorReadings(
            @PathVariable String sensorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SensorReadingResponse> readings = readingService.getSensorReadings(sensorId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Readings retrieved", readings));
    }

    @GetMapping("/sensors/{sensorId}/readings/history")
    public ResponseEntity<ApiResponse<List<SensorReadingResponse>>> getSensorHistory(
            @PathVariable String sensorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        List<SensorReadingResponse> history = readingService.getSensorHistory(sensorId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Sensor telemetry history retrieved", history));
    }

    @GetMapping("/rooms/{roomId}/readings/latest")
    public ResponseEntity<ApiResponse<List<SensorReadingResponse>>> getLatestRoomReadings(@PathVariable String roomId) {
        List<SensorReadingResponse> readings = readingService.getLatestRoomReadings(roomId);
        return ResponseEntity.ok(ApiResponse.success("Latest room sensor readings retrieved", readings));
    }

    @GetMapping("/buildings/{buildingId}/readings/latest")
    public ResponseEntity<ApiResponse<List<SensorReadingResponse>>> getLatestBuildingReadings(@PathVariable String buildingId) {
        List<SensorReadingResponse> readings = readingService.getLatestBuildingReadings(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Latest building sensor readings retrieved", readings));
    }
}
