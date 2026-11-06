package com.smartbuilding.building.controller;

import com.smartbuilding.building.dto.FloorRequest;
import com.smartbuilding.building.dto.FloorResponse;
import com.smartbuilding.building.service.FloorService;
import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class FloorController {

    private final FloorService floorService;

    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }

    @GetMapping("/buildings/{buildingId}/floors")
    public ResponseEntity<ApiResponse<List<FloorResponse>>> getFloorsByBuilding(
            @PathVariable String buildingId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<FloorResponse> floors = floorService.getFloorsByBuilding(buildingId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Floors retrieved successfully", floors));
    }

    @PostMapping("/buildings/{buildingId}/floors")
    public ResponseEntity<ApiResponse<FloorResponse>> createFloor(
            @PathVariable String buildingId,
            @Valid @RequestBody FloorRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        FloorResponse created = floorService.createFloor(buildingId, request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Floor created successfully", created));
    }

    @GetMapping("/floors/{floorId}")
    public ResponseEntity<ApiResponse<FloorResponse>> getFloorById(
            @PathVariable String floorId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        FloorResponse floor = floorService.getFloorById(floorId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Floor retrieved successfully", floor));
    }

    @PutMapping("/floors/{floorId}")
    public ResponseEntity<ApiResponse<FloorResponse>> updateFloor(
            @PathVariable String floorId,
            @Valid @RequestBody FloorRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        FloorResponse updated = floorService.updateFloor(floorId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Floor updated successfully", updated));
    }

    @DeleteMapping("/floors/{floorId}")
    public ResponseEntity<ApiResponse<Void>> deleteFloor(
            @PathVariable String floorId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        floorService.deleteFloor(floorId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Floor deleted successfully", null));
    }
}

