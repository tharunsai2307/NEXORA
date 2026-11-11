package com.smartbuilding.simulation.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.simulation.dto.ScenarioRequest;
import com.smartbuilding.simulation.dto.SimulationScenarioResponse;
import com.smartbuilding.simulation.dto.SimulationSessionResponse;
import com.smartbuilding.simulation.dto.StartSimulationRequest;
import com.smartbuilding.simulation.service.SimulationEngineService;
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
public class SimulationController {

    private final SimulationEngineService simulationService;

    public SimulationController(SimulationEngineService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/simulations/start")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SimulationSessionResponse>> startSimulation(
            @Valid @RequestBody StartSimulationRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String startedBy = userPrincipal != null ? userPrincipal.getId() : "ANONYMOUS";
        SimulationSessionResponse session = simulationService.startSimulation(request, startedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Simulation session started successfully", session));
    }

    @PostMapping("/simulations/{sessionId}/pause")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SimulationSessionResponse>> pauseSimulation(@PathVariable String sessionId) {
        SimulationSessionResponse session = simulationService.pauseSimulation(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Simulation session paused", session));
    }

    @PostMapping("/simulations/{sessionId}/resume")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SimulationSessionResponse>> resumeSimulation(@PathVariable String sessionId) {
        SimulationSessionResponse session = simulationService.resumeSimulation(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Simulation session resumed", session));
    }

    @PostMapping("/simulations/{sessionId}/stop")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SimulationSessionResponse>> stopSimulation(@PathVariable String sessionId) {
        SimulationSessionResponse session = simulationService.stopSimulation(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Simulation session stopped", session));
    }

    @GetMapping("/simulations/active")
    public ResponseEntity<ApiResponse<Object>> getActiveSimulation(
            @RequestParam(required = false) String buildingId) {
        if (buildingId != null && !buildingId.trim().isEmpty()) {
            SimulationSessionResponse session = simulationService.getActiveSession(buildingId);
            return ResponseEntity.ok(ApiResponse.success("Active session retrieved", session));
        }
        List<SimulationSessionResponse> activeSessions = simulationService.getAllActiveSessions();
        return ResponseEntity.ok(ApiResponse.success("All active sessions retrieved", activeSessions));
    }

    @GetMapping("/simulations/history")
    public ResponseEntity<ApiResponse<List<SimulationSessionResponse>>> getSimulationHistory() {
        List<SimulationSessionResponse> history = simulationService.getSessionHistory();
        return ResponseEntity.ok(ApiResponse.success("Simulation history retrieved", history));
    }

    @GetMapping("/simulations/scenarios")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableScenarios() {
        List<Map<String, Object>> scenarios = simulationService.getAvailableScenarios();
        return ResponseEntity.ok(ApiResponse.success("Available simulation scenarios retrieved", scenarios));
    }

    @PostMapping("/simulations/{sessionId}/scenarios")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SimulationScenarioResponse>> activateScenario(
            @PathVariable String sessionId,
            @Valid @RequestBody ScenarioRequest request) {
        SimulationScenarioResponse response = simulationService.activateScenario(sessionId, request);
        return ResponseEntity.ok(ApiResponse.success("Simulation scenario activated", response));
    }

    @PostMapping("/scenarios/{scenarioId}/stop")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<SimulationScenarioResponse>> stopScenario(@PathVariable String scenarioId) {
        SimulationScenarioResponse response = simulationService.stopScenario(scenarioId);
        return ResponseEntity.ok(ApiResponse.success("Simulation scenario stopped and reverted to NORMAL", response));
    }
}
