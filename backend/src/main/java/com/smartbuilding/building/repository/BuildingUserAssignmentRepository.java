package com.smartbuilding.building.repository;

import com.smartbuilding.building.model.BuildingUserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingUserAssignmentRepository extends JpaRepository<BuildingUserAssignment, String> {
    List<BuildingUserAssignment> findByBuildingId(String buildingId);
    List<BuildingUserAssignment> findByUserId(String userId);
    Optional<BuildingUserAssignment> findByBuildingIdAndUserId(String buildingId, String userId);
    boolean existsByBuildingIdAndUserId(String buildingId, String userId);
    void deleteByBuildingIdAndUserId(String buildingId, String userId);
    void deleteByBuildingId(String buildingId);
    void deleteByUserId(String userId);
}

