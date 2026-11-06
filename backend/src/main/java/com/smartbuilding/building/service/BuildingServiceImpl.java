package com.smartbuilding.building.service;

import com.smartbuilding.audit.service.AuditService;
import com.smartbuilding.building.dto.BuildingRequest;
import com.smartbuilding.building.dto.BuildingResponse;
import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.BuildingStatus;
import com.smartbuilding.building.model.BuildingType;
import com.smartbuilding.building.model.BuildingUserAssignment;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.BuildingUserAssignmentRepository;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.dto.UserResponse;
import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.model.User;
import com.smartbuilding.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final BuildingUserAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final BuildingAccessService accessService;
    private final AuditService auditService;

    public BuildingServiceImpl(
            BuildingRepository buildingRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            BuildingUserAssignmentRepository assignmentRepository,
            UserRepository userRepository,
            BuildingAccessService accessService,
            AuditService auditService) {
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingResponse> getBuildings(String query, BuildingStatus status, BuildingType type, UserDetailsImpl user) {
        String trimmedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        List<Building> buildings;
        if (user.getRole() == Role.SYSTEM_ADMIN || user.getRole() == Role.SAFETY_OFFICER) {
            buildings = buildingRepository.searchBuildings(trimmedQuery, status, type);
        } else {
            // Check if user has explicit assignments
            boolean hasAssignments = !assignmentRepository.findByUserId(user.getId()).isEmpty();
            if (hasAssignments) {
                buildings = buildingRepository.findAssignedBuildings(user.getId(), trimmedQuery, status, type);
            } else {
                buildings = buildingRepository.searchBuildings(trimmedQuery, status, type);
            }
        }

        return buildings.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingResponse getBuildingById(String id, UserDetailsImpl user) {
        accessService.requireBuildingAccess(id, user);
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + id));
        return toResponse(building);
    }

    @Override
    @Transactional
    public BuildingResponse createBuilding(BuildingRequest request, UserDetailsImpl user) {
        if (user.getRole() == Role.SAFETY_OFFICER) {
            throw ServiceException.forbidden("Safety Officers cannot create buildings");
        }

        String buildingCode = request.buildingCode().trim().toUpperCase();
        if (buildingRepository.existsByBuildingCode(buildingCode)) {
            throw ServiceException.conflict("Building code '" + buildingCode + "' is already in use");
        }

        String buildingId = UUID.randomUUID().toString();
        Building building = new Building(
                buildingId,
                request.name().trim(),
                buildingCode,
                request.description() != null ? request.description().trim() : null,
                request.address().trim(),
                request.buildingType(),
                request.status() != null ? request.status() : BuildingStatus.ACTIVE
        );

        Building savedBuilding = buildingRepository.save(building);

        // Assign creator to building
        User creator = userRepository.findById(user.getId()).orElse(null);
        if (creator != null) {
            BuildingUserAssignment assignment = new BuildingUserAssignment(
                    UUID.randomUUID().toString(),
                    savedBuilding,
                    creator
            );
            assignmentRepository.save(assignment);
        }

        auditService.log(
                user.getId(),
                "BUILDING_CREATED",
                "BUILDING",
                savedBuilding.getId(),
                "Created building " + savedBuilding.getName() + " (" + savedBuilding.getBuildingCode() + ")"
        );

        return toResponse(savedBuilding);
    }

    @Override
    @Transactional
    public BuildingResponse updateBuilding(String id, BuildingRequest request, UserDetailsImpl user) {
        accessService.requireBuildingModification(id, user);

        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + id));

        String newCode = request.buildingCode().trim().toUpperCase();
        if (!building.getBuildingCode().equalsIgnoreCase(newCode) && buildingRepository.existsByBuildingCode(newCode)) {
            throw ServiceException.conflict("Building code '" + newCode + "' is already in use");
        }

        building.setName(request.name().trim());
        building.setBuildingCode(newCode);
        building.setBuildingType(request.buildingType());
        building.setAddress(request.address().trim());
        if (request.status() != null) {
            building.setStatus(request.status());
        }
        building.setDescription(request.description() != null ? request.description().trim() : null);

        Building savedBuilding = buildingRepository.save(building);

        auditService.log(
                user.getId(),
                "BUILDING_UPDATED",
                "BUILDING",
                savedBuilding.getId(),
                "Updated building details for " + savedBuilding.getName()
        );

        return toResponse(savedBuilding);
    }

    @Override
    @Transactional
    public void deleteBuilding(String id, UserDetailsImpl user) {
        accessService.requireBuildingModification(id, user);

        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + id));

        long floorCount = floorRepository.countByBuildingId(id);
        if (floorCount > 0) {
            throw ServiceException.badRequest("Cannot delete building with " + floorCount + " registered floors. Please delete all floors and rooms first.");
        }

        assignmentRepository.deleteByBuildingId(id);
        buildingRepository.delete(building);

        auditService.log(
                user.getId(),
                "BUILDING_DELETED",
                "BUILDING",
                id,
                "Deleted building " + building.getName() + " (" + building.getBuildingCode() + ")"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAssignedUsers(String buildingId, UserDetailsImpl user) {
        accessService.requireBuildingAccess(buildingId, user);
        return assignmentRepository.findByBuildingId(buildingId).stream()
                .map(assignment -> UserResponse.fromEntity(assignment.getUser()))
                .toList();
    }

    @Override
    @Transactional
    public void assignUser(String buildingId, String userId, UserDetailsImpl user) {
        if (user.getRole() != Role.SYSTEM_ADMIN) {
            throw ServiceException.forbidden("Only System Administrators can manage building-user assignments");
        }

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> ServiceException.notFound("User not found with ID: " + userId));

        if (assignmentRepository.existsByBuildingIdAndUserId(buildingId, userId)) {
            return; // Already assigned
        }

        BuildingUserAssignment assignment = new BuildingUserAssignment(
                UUID.randomUUID().toString(),
                building,
                targetUser
        );
        assignmentRepository.save(assignment);

        auditService.log(
                user.getId(),
                "BUILDING_USER_ASSIGNED",
                "BUILDING",
                buildingId,
                "Assigned user " + targetUser.getEmail() + " to building " + building.getName()
        );
    }

    @Override
    @Transactional
    public void unassignUser(String buildingId, String userId, UserDetailsImpl user) {
        if (user.getRole() != Role.SYSTEM_ADMIN) {
            throw ServiceException.forbidden("Only System Administrators can manage building-user assignments");
        }

        assignmentRepository.deleteByBuildingIdAndUserId(buildingId, userId);

        auditService.log(
                user.getId(),
                "BUILDING_USER_UNASSIGNED",
                "BUILDING",
                buildingId,
                "Unassigned user " + userId + " from building " + buildingId
        );
    }

    private BuildingResponse toResponse(Building building) {
        long floorCount = floorRepository.countByBuildingId(building.getId());
        long roomCount = roomRepository.countByFloorBuildingId(building.getId());
        int totalCapacity = roomRepository.sumCapacityByBuildingId(building.getId());
        int currentOccupancy = roomRepository.sumOccupancyByBuildingId(building.getId());
        return BuildingResponse.fromEntity(building, floorCount, roomCount, totalCapacity, currentOccupancy);
    }
}

