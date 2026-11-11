package com.smartbuilding.ai.repository;

import com.smartbuilding.ai.model.RiskAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, String> {

    List<RiskAssessment> findByRoomIdOrderByAssessedAtDesc(String roomId);

    Optional<RiskAssessment> findFirstByRoomIdOrderByAssessedAtDesc(String roomId);

    Page<RiskAssessment> findByRoomFloorBuildingIdOrderByAssessedAtDesc(String buildingId, Pageable pageable);

    @Query("SELECT r FROM RiskAssessment r WHERE r.room.floor.building.id = :buildingId AND r.riskLevel = :riskLevel ORDER BY r.assessedAt DESC")
    Page<RiskAssessment> findByBuildingAndLevel(@Param("buildingId") String buildingId, @Param("riskLevel") String riskLevel, Pageable pageable);

    @Query("SELECT AVG(r.riskScore) FROM RiskAssessment r WHERE r.room.floor.building.id = :buildingId")
    Double getAverageRiskScoreByBuildingId(@Param("buildingId") String buildingId);

    long countByRoomFloorBuildingIdAndRiskLevel(String buildingId, String riskLevel);
}

