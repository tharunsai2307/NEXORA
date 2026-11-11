package com.smartbuilding.emergency.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.emergency.dto.*;
import com.smartbuilding.emergency.service.EvacuationRoutingService;
import com.smartbuilding.emergency.service.EvacuationSessionService;
import com.smartbuilding.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class EvacuationController {

    private final EvacuationSessionService sessionService;
    private final EvacuationRoutingService routingService;

    public EvacuationController(EvacuationSessionService sessionService,
                                EvacuationRoutingService routingService) {
        this.sessionService = sessionService;
        this.routingService = routingService;
    }

    @PostMapping("/evacuations/buildings/{buildingId}/start")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<EvacuationSessionResponse>> startEvacuation(
            @PathVariable String buildingId,
            @RequestBody(required = false) StartEvacuationRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Operator";
        EvacuationSessionResponse session = sessionService.startEvacuation(buildingId, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Evacuation session initiated", session));
    }

    @GetMapping("/evacuations/{id}")
    public ResponseEntity<ApiResponse<EvacuationSessionResponse>> getSession(@PathVariable String id) {
        EvacuationSessionResponse session = sessionService.getSession(id);
        return ResponseEntity.ok(ApiResponse.success("Evacuation session retrieved", session));
    }

    @PostMapping("/evacuations/{id}/pause")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<EvacuationSessionResponse>> pauseEvacuation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Operator";
        EvacuationSessionResponse session = sessionService.pauseEvacuation(id, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Evacuation session paused", session));
    }

    @PostMapping("/evacuations/{id}/resume")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<EvacuationSessionResponse>> resumeEvacuation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Operator";
        EvacuationSessionResponse session = sessionService.resumeEvacuation(id, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Evacuation session resumed", session));
    }

    @PostMapping("/evacuations/{id}/cancel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<EvacuationSessionResponse>> cancelEvacuation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Operator";
        EvacuationSessionResponse session = sessionService.cancelEvacuation(id, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Evacuation session cancelled", session));
    }

    @PostMapping("/evacuations/{id}/step")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<EvacuationSessionResponse>> stepEvacuationProgress(
            @PathVariable String id) {
        EvacuationSessionResponse session = sessionService.stepEvacuationProgress(id);
        return ResponseEntity.ok(ApiResponse.success("Evacuation step simulated successfully", session));
    }

    @GetMapping("/evacuations/{id}/routes")
    public ResponseEntity<ApiResponse<List<EvacuationRouteResponse>>> getSessionRoutes(
            @PathVariable String id) {
        List<EvacuationRouteResponse> routes = routingService.getSessionRoutes(id);
        return ResponseEntity.ok(ApiResponse.success("Evacuation session routes retrieved", routes));
    }

    @GetMapping("/evacuations/buildings/{buildingId}/active")
    public ResponseEntity<ApiResponse<EvacuationSessionResponse>> getActiveSession(
            @PathVariable String buildingId) {
        EvacuationSessionResponse session = sessionService.getActiveSession(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Active session retrieved", session));
    }

    @GetMapping("/evacuations/buildings/{buildingId}/sessions")
    public ResponseEntity<ApiResponse<List<EvacuationSessionResponse>>> getBuildingSessions(
            @PathVariable String buildingId) {
        List<EvacuationSessionResponse> sessions = sessionService.getBuildingSessions(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Building evacuation sessions retrieved", sessions));
    }

    @GetMapping("/evacuation/buildings/{buildingId}/graph")
    public ResponseEntity<ApiResponse<EvacuationGraphResponse>> getBuildingGraph(
            @PathVariable String buildingId) {
        EvacuationGraphResponse graph = routingService.getBuildingGraph(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Evacuation graph retrieved", graph));
    }

    @PostMapping("/evacuation/buildings/{buildingId}/graph/initialize")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<EvacuationGraphResponse>> initializeDefaultGraph(
            @PathVariable String buildingId) {
        EvacuationGraphResponse graph = routingService.initializeDefaultGraph(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Default evacuation graph initialized", graph));
    }

    @PostMapping("/evacuation/connections/{connectionId}/toggle-block")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<EvacuationConnectionDto>> toggleConnectionBlock(
            @PathVariable String connectionId) {
        EvacuationConnectionDto conn = routingService.toggleConnectionBlock(connectionId);
        return ResponseEntity.ok(ApiResponse.success("Connection block toggled", conn));
    }

    @GetMapping("/evacuation/buildings/{buildingId}/routes")
    public ResponseEntity<ApiResponse<List<EvacuationRouteResponse>>> getBuildingRoutes(
            @PathVariable String buildingId) {
        List<EvacuationRouteResponse> routes = routingService.getBuildingRoutes(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Building evacuation routes retrieved", routes));
    }

    @PostMapping("/evacuation/buildings/{buildingId}/routes/recalculate")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<List<EvacuationRouteResponse>>> recalculateBuildingRoutes(
            @PathVariable String buildingId) {
        List<EvacuationRouteResponse> routes = routingService.calculateRoutesForBuilding(buildingId, null);
        return ResponseEntity.ok(ApiResponse.success("Evacuation routes recalculated successfully", routes));
    }
}
