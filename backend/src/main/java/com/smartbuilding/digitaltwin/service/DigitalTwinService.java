package com.smartbuilding.digitaltwin.service;

import com.smartbuilding.digitaltwin.dto.DigitalTwinBuildingResponse;
import com.smartbuilding.digitaltwin.dto.DigitalTwinRoomResponse;
import com.smartbuilding.digitaltwin.dto.RoomLayoutUpdateRequest;

public interface DigitalTwinService {

    DigitalTwinBuildingResponse getDigitalTwinBuilding(String buildingId);

    DigitalTwinRoomResponse updateRoomLayout(String roomId, RoomLayoutUpdateRequest request);
}

