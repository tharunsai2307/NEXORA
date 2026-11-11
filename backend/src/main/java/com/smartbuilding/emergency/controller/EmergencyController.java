package com.smartbuilding.emergency.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.emergency.dto.BuildingEmergencyStatusResponse;
import com.smartbuilding.emergency.service.EvacuationSessionService;
import com.smartbuilding.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/emergency")
public class EmergencyController {

    private final EvacuationSessionService sessionService;

    public EmergencyController(EvacuationSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/buildings/{buildingId}/status")
    public ResponseEntity<ApiResponse<BuildingEmergencyStatusResponse>> getBuildingEmergencyStatus(
            @PathVariable String buildingId) {
        BuildingEmergencyStatusResponse status = sessionService.getBuildingEmergencyStatus(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Building emergency status retrieved", status));
    }

    @PostMapping("/buildings/{buildingId}/activate")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER')")
    public ResponseEntity<ApiResponse<BuildingEmergencyStatusResponse>> activateBuildingEmergency(
            @PathVariable String buildingId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Emergency Protocol Triggered";
        String userId = userPrincipal != null ? userPrincipal.getId() : "SYSTEM";
        String userName = userPrincipal != null ? userPrincipal.getFullName() : "Emergency Operator";

        BuildingEmergencyStatusResponse status = sessionService.activateBuildingEmergency(buildingId, reason, userId, userName);
        return ResponseEntity.ok(ApiResponse.success("Emergency protocol activated for building", status));
    }
}
