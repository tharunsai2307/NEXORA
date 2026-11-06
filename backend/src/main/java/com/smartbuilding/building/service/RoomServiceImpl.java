package com.smartbuilding.building.service;

import com.smartbuilding.audit.service.AuditService;
import com.smartbuilding.building.dto.RoomRequest;
import com.smartbuilding.building.dto.RoomResponse;
import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.model.SafetyStatus;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.UserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final BuildingAccessService accessService;
    private final AuditService auditService;

    public RoomServiceImpl(
            RoomRepository roomRepository,
            FloorRepository floorRepository,
            BuildingAccessService accessService,
            AuditService auditService) {
        this.roomRepository = roomRepository;
        this.floorRepository = floorRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByFloor(String floorId, UserDetailsImpl user) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> ServiceException.notFound("Floor not found with ID: " + floorId));
        accessService.requireBuildingAccess(floor.getBuilding().getId(), user);

        return roomRepository.findByFloorIdOrderByRoomCodeAsc(floorId).stream()
                .map(RoomResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(String roomId, UserDetailsImpl user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));
        accessService.requireBuildingAccess(room.getFloor().getBuilding().getId(), user);
        return RoomResponse.fromEntity(room);
    }

    @Override
    @Transactional
    public RoomResponse createRoom(String floorId, RoomRequest request, UserDetailsImpl user) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> ServiceException.notFound("Floor not found with ID: " + floorId));

        accessService.requireBuildingModification(floor.getBuilding().getId(), user);

        String roomCode = request.roomCode().trim().toUpperCase();
        if (roomRepository.existsByFloorIdAndRoomCode(floorId, roomCode)) {
            throw ServiceException.conflict("Room code '" + roomCode + "' already exists on this floor");
        }

        String roomId = UUID.randomUUID().toString();
        Room room = new Room(
                roomId,
                floor,
                request.roomName().trim(),
                roomCode,
                request.roomType(),
                request.description() != null ? request.description().trim() : null,
                request.maximumCapacity(),
                request.currentOccupancy() != null ? request.currentOccupancy() : 0,
                request.safetyStatus() != null ? request.safetyStatus() : SafetyStatus.NORMAL,
                request.posX() != null ? request.posX() : 0.0,
                request.posY() != null ? request.posY() : 0.0,
                request.posZ() != null ? request.posZ() : 0.0,
                request.width() != null ? request.width() : 10.0,
                request.depth() != null ? request.depth() : 8.0,
                request.height() != null ? request.height() : 3.5,
                request.active() != null ? request.active() : true
        );

        Room savedRoom = roomRepository.save(room);

        auditService.log(
                user.getId(),
                "ROOM_CREATED",
                "ROOM",
                savedRoom.getId(),
                "Created room " + savedRoom.getRoomName() + " (" + savedRoom.getRoomCode() + ") on floor " + floor.getFloorName()
        );

        return RoomResponse.fromEntity(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(String roomId, RoomRequest request, UserDetailsImpl user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));

        accessService.requireBuildingModification(room.getFloor().getBuilding().getId(), user);

        String roomCode = request.roomCode().trim().toUpperCase();
        if (!room.getRoomCode().equalsIgnoreCase(roomCode) &&
                roomRepository.existsByFloorIdAndRoomCode(room.getFloor().getId(), roomCode)) {
            throw ServiceException.conflict("Room code '" + roomCode + "' already exists on this floor");
        }

        room.setRoomName(request.roomName().trim());
        room.setRoomCode(roomCode);
        room.setRoomType(request.roomType());
        room.setDescription(request.description() != null ? request.description().trim() : null);
        room.setMaximumCapacity(request.maximumCapacity());
        if (request.currentOccupancy() != null) {
            room.setCurrentOccupancy(request.currentOccupancy());
        }
        if (request.safetyStatus() != null) {
            room.setSafetyStatus(request.safetyStatus());
        }
        if (request.posX() != null) room.setPosX(request.posX());
        if (request.posY() != null) room.setPosY(request.posY());
        if (request.posZ() != null) room.setPosZ(request.posZ());
        if (request.width() != null) room.setWidth(request.width());
        if (request.depth() != null) room.setDepth(request.depth());
        if (request.height() != null) room.setHeight(request.height());
        if (request.active() != null) room.setActive(request.active());

        Room savedRoom = roomRepository.save(room);

        auditService.log(
                user.getId(),
                "ROOM_UPDATED",
                "ROOM",
                savedRoom.getId(),
                "Updated room details for " + savedRoom.getRoomName()
        );

        return RoomResponse.fromEntity(savedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(String roomId, UserDetailsImpl user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));

        accessService.requireBuildingModification(room.getFloor().getBuilding().getId(), user);

        roomRepository.delete(room);

        auditService.log(
                user.getId(),
                "ROOM_DELETED",
                "ROOM",
                roomId,
                "Deleted room " + room.getRoomName() + " (" + room.getRoomCode() + ")"
        );
    }
}

