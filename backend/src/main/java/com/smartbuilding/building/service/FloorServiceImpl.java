package com.smartbuilding.building.service;

import com.smartbuilding.audit.service.AuditService;
import com.smartbuilding.building.dto.FloorRequest;
import com.smartbuilding.building.dto.FloorResponse;
import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.FloorStatus;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.UserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FloorServiceImpl implements FloorService {

    private final FloorRepository floorRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final BuildingAccessService accessService;
    private final AuditService auditService;

    public FloorServiceImpl(
            FloorRepository floorRepository,
            BuildingRepository buildingRepository,
            RoomRepository roomRepository,
            BuildingAccessService accessService,
            AuditService auditService) {
        this.floorRepository = floorRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FloorResponse> getFloorsByBuilding(String buildingId, UserDetailsImpl user) {
        accessService.requireBuildingAccess(buildingId, user);
        if (!buildingRepository.existsById(buildingId)) {
            throw ServiceException.notFound("Building not found with ID: " + buildingId);
        }
        return floorRepository.findByBuildingIdOrderByFloorNumberAsc(buildingId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FloorResponse getFloorById(String floorId, UserDetailsImpl user) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> ServiceException.notFound("Floor not found with ID: " + floorId));
        accessService.requireBuildingAccess(floor.getBuilding().getId(), user);
        return toResponse(floor);
    }

    @Override
    @Transactional
    public FloorResponse createFloor(String buildingId, FloorRequest request, UserDetailsImpl user) {
        accessService.requireBuildingModification(buildingId, user);

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        if (floorRepository.existsByBuildingIdAndFloorNumber(buildingId, request.floorNumber())) {
            throw ServiceException.conflict("Floor number " + request.floorNumber() + " already exists in this building");
        }

        String floorId = UUID.randomUUID().toString();
        Floor floor = new Floor(
                floorId,
                building,
                request.floorName().trim(),
                request.floorNumber(),
                request.description() != null ? request.description().trim() : null,
                request.layoutWidth() != null ? request.layoutWidth() : 60.0,
                request.layoutDepth() != null ? request.layoutDepth() : 40.0,
                request.status() != null ? request.status() : FloorStatus.ACTIVE
        );

        Floor savedFloor = floorRepository.save(floor);

        auditService.log(
                user.getId(),
                "FLOOR_CREATED",
                "FLOOR",
                savedFloor.getId(),
                "Created floor " + savedFloor.getFloorName() + " (Level " + savedFloor.getFloorNumber() + ") in building " + building.getName()
        );

        return toResponse(savedFloor);
    }

    @Override
    @Transactional
    public FloorResponse updateFloor(String floorId, FloorRequest request, UserDetailsImpl user) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> ServiceException.notFound("Floor not found with ID: " + floorId));

        accessService.requireBuildingModification(floor.getBuilding().getId(), user);

        if (floor.getFloorNumber() != request.floorNumber() &&
                floorRepository.existsByBuildingIdAndFloorNumber(floor.getBuilding().getId(), request.floorNumber())) {
            throw ServiceException.conflict("Floor number " + request.floorNumber() + " already exists in this building");
        }

        floor.setFloorName(request.floorName().trim());
        floor.setFloorNumber(request.floorNumber());
        floor.setDescription(request.description() != null ? request.description().trim() : null);
        if (request.layoutWidth() != null) {
            floor.setLayoutWidth(request.layoutWidth());
        }
        if (request.layoutDepth() != null) {
            floor.setLayoutDepth(request.layoutDepth());
        }
        if (request.status() != null) {
            floor.setStatus(request.status());
        }

        Floor savedFloor = floorRepository.save(floor);

        auditService.log(
                user.getId(),
                "FLOOR_UPDATED",
                "FLOOR",
                savedFloor.getId(),
                "Updated floor details for " + savedFloor.getFloorName()
        );

        return toResponse(savedFloor);
    }

    @Override
    @Transactional
    public void deleteFloor(String floorId, UserDetailsImpl user) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> ServiceException.notFound("Floor not found with ID: " + floorId));

        accessService.requireBuildingModification(floor.getBuilding().getId(), user);

        long roomCount = roomRepository.countByFloorId(floorId);
        if (roomCount > 0) {
            throw ServiceException.badRequest("Cannot delete floor with " + roomCount + " existing rooms. Please remove all rooms first.");
        }

        floorRepository.delete(floor);

        auditService.log(
                user.getId(),
                "FLOOR_DELETED",
                "FLOOR",
                floorId,
                "Deleted floor " + floor.getFloorName() + " from building " + floor.getBuilding().getName()
        );
    }

    private FloorResponse toResponse(Floor floor) {
        long roomCount = roomRepository.countByFloorId(floor.getId());
        int totalCapacity = roomRepository.sumCapacityByFloorId(floor.getId());
        int currentOccupancy = roomRepository.sumOccupancyByFloorId(floor.getId());
        return FloorResponse.fromEntity(floor, roomCount, totalCapacity, currentOccupancy);
    }
}

