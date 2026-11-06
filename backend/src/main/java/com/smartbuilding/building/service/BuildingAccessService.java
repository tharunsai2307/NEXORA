package com.smartbuilding.building.service;

import com.smartbuilding.building.repository.BuildingUserAssignmentRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.security.UserDetailsImpl;
import com.smartbuilding.user.model.Role;
import org.springframework.stereotype.Service;

@Service
public class BuildingAccessService {

    private final BuildingUserAssignmentRepository assignmentRepository;

    public BuildingAccessService(BuildingUserAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public boolean canAccessBuilding(String buildingId, UserDetailsImpl user) {
        if (user.getRole() == Role.SYSTEM_ADMIN) {
            return true;
        }
        if (user.getRole() == Role.SAFETY_OFFICER) {
            return true;
        }
        // BUILDING_MANAGER: can access if assigned or if they have global access
        boolean hasSpecificAssignments = !assignmentRepository.findByUserId(user.getId()).isEmpty();
        if (!hasSpecificAssignments) {
            return true; // If no restrictive assignments, managers can access
        }
        return assignmentRepository.existsByBuildingIdAndUserId(buildingId, user.getId());
    }

    public void requireBuildingAccess(String buildingId, UserDetailsImpl user) {
        if (!canAccessBuilding(buildingId, user)) {
            throw ServiceException.forbidden("You do not have permission to access or modify this building");
        }
    }

    public void requireBuildingModification(String buildingId, UserDetailsImpl user) {
        if (user.getRole() == Role.SAFETY_OFFICER) {
            throw ServiceException.forbidden("Safety Officers have read-only access to building infrastructure");
        }
        requireBuildingAccess(buildingId, user);
    }
}

