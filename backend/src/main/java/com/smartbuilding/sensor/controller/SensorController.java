package com.smartbuilding.sensor.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.sensor.dto.*;
import com.smartbuilding.sensor.model.SensorStatus;
import com.smartbuilding.sensor.model.SensorType;
import com.smartbuilding.sensor.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping("/rooms/{roomId}/sensors")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SensorResponse>> createSensor(
            @PathVariable String roomId,
            @Valid @RequestBody CreateSensorRequest request) {
        SensorResponse response = sensorService.createSensor(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sensor registered successfully", response));
    }

    @PostMapping("/rooms/{roomId}/sensors/provision")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<List<SensorResponse>>> provisionRoomSensors(@PathVariable String roomId) {
        List<SensorResponse> sensors = sensorService.provisionDefaultSensors(roomId);
        return ResponseEntity.ok(ApiResponse.success("Default 5-sensor suite provisioned successfully", sensors));
    }

    @PostMapping("/buildings/{buildingId}/sensors/provision")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<List<SensorResponse>>> provisionBuildingSensors(@PathVariable String buildingId) {
        List<SensorResponse> sensors = sensorService.provisionBuildingSensors(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Building sensor suites provisioned successfully", sensors));
    }

    @GetMapping("/rooms/{roomId}/sensors")
    public ResponseEntity<ApiResponse<List<SensorResponse>>> getSensorsByRoom(@PathVariable String roomId) {
        List<SensorResponse> sensors = sensorService.getSensorsByRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("Room sensors retrieved", sensors));
    }

    @GetMapping("/sensors")
    public ResponseEntity<ApiResponse<List<SensorResponse>>> getSensors(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) String floorId,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) SensorType type,
            @RequestParam(required = false) SensorStatus status) {
        List<SensorResponse> sensors = sensorService.getSensors(buildingId, floorId, roomId, type, status);
        return ResponseEntity.ok(ApiResponse.success("Sensors retrieved", sensors));
    }

    @GetMapping("/sensors/{sensorId}")
    public ResponseEntity<ApiResponse<SensorResponse>> getSensorById(@PathVariable String sensorId) {
        SensorResponse sensor = sensorService.getSensorById(sensorId);
        return ResponseEntity.ok(ApiResponse.success("Sensor details retrieved", sensor));
    }

    @PutMapping("/sensors/{sensorId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SensorResponse>> updateSensor(
            @PathVariable String sensorId,
            @Valid @RequestBody UpdateSensorRequest request) {
        SensorResponse response = sensorService.updateSensor(sensorId, request);
        return ResponseEntity.ok(ApiResponse.success("Sensor updated successfully", response));
    }

    @PatchMapping("/sensors/{sensorId}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SensorResponse>> updateSensorStatus(
            @PathVariable String sensorId,
            @RequestBody Map<String, SensorStatus> payload) {
        SensorStatus status = payload.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing 'status' in payload"));
        }
        SensorResponse response = sensorService.updateSensorStatus(sensorId, status);
        return ResponseEntity.ok(ApiResponse.success("Sensor status updated", response));
    }

    @DeleteMapping("/sensors/{sensorId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<Void> deleteSensor(@PathVariable String sensorId) {
        sensorService.deleteSensor(sensorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sensors/{sensorId}/readings/manual")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<SensorReadingResponse>> recordManualReading(
            @PathVariable String sensorId,
            @Valid @RequestBody ManualReadingRequest request) {
        SensorReadingResponse reading = sensorService.recordManualReading(sensorId, request.getReadingValue());
        return ResponseEntity.ok(ApiResponse.success("Manual reading recorded", reading));
    }

    @GetMapping("/sensors/stats")
    public ResponseEntity<ApiResponse<SensorStatsResponse>> getSensorStats() {
        SensorStatsResponse stats = sensorService.getSensorStats();
        return ResponseEntity.ok(ApiResponse.success("Sensor statistics retrieved", stats));
    }
}

