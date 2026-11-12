package com.smartbuilding.health.controller;

import com.smartbuilding.ai.repository.AnomalyDetectionRepository;
import com.smartbuilding.ai.repository.RiskAssessmentRepository;
import com.smartbuilding.building.dto.DashboardStatsResponse;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.BuildingUserAssignmentRepository;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.building.service.DashboardStatsService;
import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.config.DatabaseDataInitializer;
import com.smartbuilding.emergency.repository.*;
import com.smartbuilding.health.dto.SystemInfoResponse;
import com.smartbuilding.health.service.HealthService;
import com.smartbuilding.sensor.repository.SensorReadingRepository;
import com.smartbuilding.sensor.repository.SensorRepository;
import com.smartbuilding.simulation.repository.SimulationScenarioRepository;
import com.smartbuilding.simulation.repository.SimulationSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final HealthService healthService;
    private final DashboardStatsService dashboardStatsService;
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorRepository sensorRepository;
    private final AnomalyDetectionRepository anomalyDetectionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final IncidentActivityRepository incidentActivityRepository;
    private final IncidentRepository incidentRepository;
    private final EvacuationConnectionRepository evacuationConnectionRepository;
    private final EvacuationNodeRepository evacuationNodeRepository;
    private final EvacuationRoomStatusRepository evacuationRoomStatusRepository;
    private final EvacuationRouteRepository evacuationRouteRepository;
    private final EvacuationSessionRepository evacuationSessionRepository;
    private final SimulationScenarioRepository simulationScenarioRepository;
    private final SimulationSessionRepository simulationSessionRepository;
    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final BuildingUserAssignmentRepository buildingUserAssignmentRepository;
    private final BuildingRepository buildingRepository;
    private final DatabaseDataInitializer databaseDataInitializer;

    public SystemController(
            HealthService healthService,
            DashboardStatsService dashboardStatsService,
            SensorReadingRepository sensorReadingRepository,
            SensorRepository sensorRepository,
            AnomalyDetectionRepository anomalyDetectionRepository,
            RiskAssessmentRepository riskAssessmentRepository,
            IncidentActivityRepository incidentActivityRepository,
            IncidentRepository incidentRepository,
            EvacuationConnectionRepository evacuationConnectionRepository,
            EvacuationNodeRepository evacuationNodeRepository,
            EvacuationRoomStatusRepository evacuationRoomStatusRepository,
            EvacuationRouteRepository evacuationRouteRepository,
            EvacuationSessionRepository evacuationSessionRepository,
            SimulationScenarioRepository simulationScenarioRepository,
            SimulationSessionRepository simulationSessionRepository,
            RoomRepository roomRepository,
            FloorRepository floorRepository,
            BuildingUserAssignmentRepository buildingUserAssignmentRepository,
            BuildingRepository buildingRepository,
            DatabaseDataInitializer databaseDataInitializer) {
        this.healthService = healthService;
        this.dashboardStatsService = dashboardStatsService;
        this.sensorReadingRepository = sensorReadingRepository;
        this.sensorRepository = sensorRepository;
        this.anomalyDetectionRepository = anomalyDetectionRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.incidentActivityRepository = incidentActivityRepository;
        this.incidentRepository = incidentRepository;
        this.evacuationConnectionRepository = evacuationConnectionRepository;
        this.evacuationNodeRepository = evacuationNodeRepository;
        this.evacuationRoomStatusRepository = evacuationRoomStatusRepository;
        this.evacuationRouteRepository = evacuationRouteRepository;
        this.evacuationSessionRepository = evacuationSessionRepository;
        this.simulationScenarioRepository = simulationScenarioRepository;
        this.simulationSessionRepository = simulationSessionRepository;
        this.roomRepository = roomRepository;
        this.floorRepository = floorRepository;
        this.buildingUserAssignmentRepository = buildingUserAssignmentRepository;
        this.buildingRepository = buildingRepository;
        this.databaseDataInitializer = databaseDataInitializer;
    }

    @GetMapping("/info")
    public ResponseEntity<SystemInfoResponse> getSystemInfo() {
        return ResponseEntity.ok(healthService.getSystemInfo());
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardStatsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved successfully", stats));
    }

    @PostMapping("/purge-telemetry")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    @Transactional
    public ResponseEntity<ApiResponse<String>> purgeTelemetry() {
        sensorReadingRepository.deleteAllInBatch();
        anomalyDetectionRepository.deleteAllInBatch();
        riskAssessmentRepository.deleteAllInBatch();
        return ResponseEntity.ok(ApiResponse.success("Historical telemetry and anomalies purged successfully", "OK"));
    }

    @PostMapping("/clear-facility-data")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<String>> clearFacilityData() {
        incidentActivityRepository.deleteAllInBatch();
        incidentRepository.deleteAllInBatch();
        evacuationConnectionRepository.deleteAllInBatch();
        evacuationNodeRepository.deleteAllInBatch();
        evacuationRoomStatusRepository.deleteAllInBatch();
        evacuationRouteRepository.deleteAllInBatch();
        evacuationSessionRepository.deleteAllInBatch();
        simulationScenarioRepository.deleteAllInBatch();
        simulationSessionRepository.deleteAllInBatch();
        sensorReadingRepository.deleteAllInBatch();
        anomalyDetectionRepository.deleteAllInBatch();
        riskAssessmentRepository.deleteAllInBatch();
        sensorRepository.deleteAllInBatch();
        roomRepository.deleteAllInBatch();
        floorRepository.deleteAllInBatch();
        buildingUserAssignmentRepository.deleteAllInBatch();
        buildingRepository.deleteAllInBatch();
        return ResponseEntity.ok(ApiResponse.success("All facilities, floors, rooms, and sensors removed", "CLEARED"));
    }

    @PostMapping("/seed-baseline-data")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<String>> seedBaselineData() {
        databaseDataInitializer.run();
        return ResponseEntity.ok(ApiResponse.success("Baseline facility topology seeded successfully", "SEEDED"));
    }
}
