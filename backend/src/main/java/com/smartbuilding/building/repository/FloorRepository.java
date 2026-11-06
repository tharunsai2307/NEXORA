package com.smartbuilding.building.repository;

import com.smartbuilding.building.model.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, String> {
    List<Floor> findByBuildingIdOrderByFloorNumberAsc(String buildingId);
    Optional<Floor> findByBuildingIdAndFloorNumber(String buildingId, int floorNumber);
    boolean existsByBuildingIdAndFloorNumber(String buildingId, int floorNumber);
    long countByBuildingId(String buildingId);
    void deleteByBuildingId(String buildingId);
}

