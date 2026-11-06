package com.smartbuilding.building.controller;

import com.smartbuilding.building.dto.RoomRequest;
import com.smartbuilding.building.dto.RoomResponse;
import com.smartbuilding.building.service.RoomService;
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
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByFloor(
            @PathVariable String floorId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        List<RoomResponse> rooms = roomService.getRoomsByFloor(floorId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", rooms));
    }

    @PostMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @PathVariable String floorId,
            @Valid @RequestBody RoomRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        RoomResponse created = roomService.createRoom(floorId, request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", created));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        RoomResponse room = roomService.getRoomById(roomId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Room retrieved successfully", room));
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable String roomId,
            @Valid @RequestBody RoomRequest request,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        RoomResponse updated = roomService.updateRoom(roomId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", updated));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserDetailsImpl userPrincipal) {
        roomService.deleteRoom(roomId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
    }
}

