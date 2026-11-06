package com.smartbuilding.building.service;

import com.smartbuilding.building.dto.FloorRequest;
import com.smartbuilding.building.dto.FloorResponse;
import com.smartbuilding.security.UserDetailsImpl;

import java.util.List;

public interface FloorService {
    List<FloorResponse> getFloorsByBuilding(String buildingId, UserDetailsImpl user);
    FloorResponse getFloorById(String floorId, UserDetailsImpl user);
    FloorResponse createFloor(String buildingId, FloorRequest request, UserDetailsImpl user);
    FloorResponse updateFloor(String floorId, FloorRequest request, UserDetailsImpl user);
    void deleteFloor(String floorId, UserDetailsImpl user);
}
