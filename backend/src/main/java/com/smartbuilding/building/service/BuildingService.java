package com.smartbuilding.building.service;

import com.smartbuilding.building.dto.BuildingRequest;
import com.smartbuilding.building.dto.BuildingResponse;
import com.smartbuilding.building.model.BuildingStatus;
import com.smartbuilding.building.model.BuildingType;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.dto.UserResponse;

import java.util.List;

public interface BuildingService {
    List<BuildingResponse> getBuildings(String query, BuildingStatus status, BuildingType type, UserDetailsImpl user);
    BuildingResponse getBuildingById(String id, UserDetailsImpl user);
    BuildingResponse createBuilding(BuildingRequest request, UserDetailsImpl user);
    BuildingResponse updateBuilding(String id, BuildingRequest request, UserDetailsImpl user);
    void deleteBuilding(String id, UserDetailsImpl user);
    List<UserResponse> getAssignedUsers(String buildingId, UserDetailsImpl user);
    void assignUser(String buildingId, String userId, UserDetailsImpl user);
    void unassignUser(String buildingId, String userId, UserDetailsImpl user);
}
