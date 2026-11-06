package com.smartbuilding.building.service;

import com.smartbuilding.building.dto.RoomRequest;
import com.smartbuilding.building.dto.RoomResponse;
import com.smartbuilding.security.UserDetailsImpl;

import java.util.List;

public interface RoomService {
    List<RoomResponse> getRoomsByFloor(String floorId, UserDetailsImpl user);
    RoomResponse getRoomById(String roomId, UserDetailsImpl user);
    RoomResponse createRoom(String floorId, RoomRequest request, UserDetailsImpl user);
    RoomResponse updateRoom(String roomId, RoomRequest request, UserDetailsImpl user);
    void deleteRoom(String roomId, UserDetailsImpl user);
}
