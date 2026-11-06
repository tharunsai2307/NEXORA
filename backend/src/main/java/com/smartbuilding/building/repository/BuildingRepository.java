package com.smartbuilding.building.repository;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.BuildingStatus;
import com.smartbuilding.building.model.BuildingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, String> {
    Optional<Building> findByBuildingCode(String buildingCode);
    boolean existsByBuildingCode(String buildingCode);

    @Query("SELECT b FROM Building b WHERE " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:type IS NULL OR b.buildingType = :type) AND " +
           "(:query IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.buildingCode) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY b.createdAt DESC")
    List<Building> searchBuildings(@Param("query") String query, @Param("status") BuildingStatus status, @Param("type") BuildingType type);

    @Query("SELECT b FROM Building b JOIN BuildingUserAssignment bua ON b.id = bua.building.id " +
           "WHERE bua.user.id = :userId AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:type IS NULL OR b.buildingType = :type) AND " +
           "(:query IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.buildingCode) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY b.createdAt DESC")
    List<Building> findAssignedBuildings(@Param("userId") String userId, @Param("query") String query, @Param("status") BuildingStatus status, @Param("type") BuildingType type);
}

