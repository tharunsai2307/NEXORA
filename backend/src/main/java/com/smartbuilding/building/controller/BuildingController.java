package com.smartbuilding.building.controller;

import com.smartbuilding.building.dto.AssignUserRequest;
import com.smartbuilding.building.dto.BuildingRequest;
import com.smartbuilding.building.dto.BuildingResponse;
import com.smartbuilding.building.model.BuildingStatus;
import com.smartbuilding.building.model.BuildingType;
import com.smartbuilding.building.service.BuildingService;
import com.smartbuilding.common.ApiResponse;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BuildingResponse>>> getBuildings(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BuildingStatus status,
            @RequestParam(required = false) BuildingType type,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<BuildingResponse> buildings = buildingService.getBuildings(query, status, type, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Buildings retrieved successfully", buildings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BuildingResponse>> getBuildingById(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        BuildingResponse building = buildingService.getBuildingById(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Building retrieved successfully", building));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BuildingResponse>> createBuilding(
            @Valid @RequestBody BuildingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        BuildingResponse created = buildingService.createBuilding(request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Building created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BuildingResponse>> updateBuilding(
            @PathVariable String id,
            @Valid @RequestBody BuildingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        BuildingResponse updated = buildingService.updateBuilding(id, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Building updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBuilding(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        buildingService.deleteBuilding(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Building deleted successfully", null));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAssignedUsers(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<UserResponse> users = buildingService.getAssignedUsers(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Assigned users retrieved successfully", users));
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<ApiResponse<Void>> assignUser(
            @PathVariable String id,
            @Valid @RequestBody AssignUserRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        buildingService.assignUser(id, request.userId(), userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("User assigned to building successfully", null));
    }

    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> unassignUser(
            @PathVariable String id,
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        buildingService.unassignUser(id, userId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("User unassigned from building successfully", null));
    }
}

