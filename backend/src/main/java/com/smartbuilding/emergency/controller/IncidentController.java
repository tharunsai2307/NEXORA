package com.smartbuilding.emergency.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.emergency.dto.*;
import com.smartbuilding.emergency.model.IncidentCategory;
import com.smartbuilding.emergency.model.IncidentSeverity;
import com.smartbuilding.emergency.model.IncidentStatus;
import com.smartbuilding.emergency.service.IncidentService;
import com.smartbuilding.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Operator";
        IncidentResponse created = incidentService.createIncident(request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Emergency incident logged successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncident(@PathVariable String id) {
        IncidentResponse incident = incidentService.getIncident(id);
        return ResponseEntity.ok(ApiResponse.success("Incident retrieved", incident));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getFilteredIncidents(
            @RequestParam(required = false) String buildingId,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) IncidentCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IncidentResponse> incidents = incidentService.getFilteredIncidents(buildingId, status, severity, category, page, size);
        return ResponseEntity.ok(ApiResponse.success("Incidents retrieved", incidents));
    }

    @GetMapping("/buildings/{buildingId}/active")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getActiveBuildingIncidents(@PathVariable String buildingId) {
        List<IncidentResponse> active = incidentService.getActiveBuildingIncidents(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Active incidents retrieved", active));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateIncidentStatusRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Operator";
        IncidentResponse updated = incidentService.updateIncidentStatus(id, request, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated", updated));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<IncidentResponse>> assignIncident(
            @PathVariable String id,
            @Valid @RequestBody AssignIncidentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Manager";
        IncidentResponse assigned = incidentService.assignIncident(id, request, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Incident assigned successfully", assigned));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<IncidentResponse>> acknowledgeIncident(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Officer";
        IncidentResponse acked = incidentService.acknowledgeIncident(id, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Incident acknowledged", acked));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<IncidentResponse>> resolveIncident(
            @PathVariable String id,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Safety Officer";
        IncidentResponse resolved = incidentService.resolveIncident(id, notes, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Incident marked resolved", resolved));
    }
}
