package com.smartbuilding.digitaltwin.controller;

import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.digitaltwin.dto.DigitalTwinBuildingResponse;
import com.smartbuilding.digitaltwin.dto.DigitalTwinRoomResponse;
import com.smartbuilding.digitaltwin.dto.RoomLayoutUpdateRequest;
import com.smartbuilding.digitaltwin.service.DigitalTwinService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/digital-twin")
public class DigitalTwinController {

    private final DigitalTwinService digitalTwinService;

    public DigitalTwinController(DigitalTwinService digitalTwinService) {
        this.digitalTwinService = digitalTwinService;
    }

    @GetMapping("/buildings/{buildingId}")
    public ResponseEntity<ApiResponse<DigitalTwinBuildingResponse>> getDigitalTwinBuilding(
            @PathVariable String buildingId) {
        DigitalTwinBuildingResponse response = digitalTwinService.getDigitalTwinBuilding(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Digital twin building telemetry loaded successfully", response));
    }

    @PutMapping("/rooms/{roomId}/layout")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BUILDING_MANAGER')")
    public ResponseEntity<ApiResponse<DigitalTwinRoomResponse>> updateRoomLayout(
            @PathVariable String roomId,
            @Valid @RequestBody RoomLayoutUpdateRequest request) {
        DigitalTwinRoomResponse response = digitalTwinService.updateRoomLayout(roomId, request);
        return ResponseEntity.ok(ApiResponse.success("Room 3D spatial layout coordinates updated successfully", response));
    }
}

