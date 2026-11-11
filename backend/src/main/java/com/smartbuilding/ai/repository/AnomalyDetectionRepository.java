package com.smartbuilding.ai.repository;

import com.smartbuilding.ai.model.AnomalyDetection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnomalyDetectionRepository extends JpaRepository<AnomalyDetection, String> {

    List<AnomalyDetection> findByRoomIdOrderByDetectedAtDesc(String roomId);

    Optional<AnomalyDetection> findFirstByRoomIdOrderByDetectedAtDesc(String roomId);

    Page<AnomalyDetection> findByRoomFloorBuildingIdOrderByDetectedAtDesc(String buildingId, Pageable pageable);

    @Query("SELECT a FROM AnomalyDetection a WHERE a.room.floor.building.id = :buildingId AND a.severity = :severity ORDER BY a.detectedAt DESC")
    Page<AnomalyDetection> findByBuildingAndSeverity(@Param("buildingId") String buildingId, @Param("severity") String severity, Pageable pageable);

    @Query("SELECT a FROM AnomalyDetection a WHERE a.room.id = :roomId AND (:severity IS NULL OR a.severity = :severity) ORDER BY a.detectedAt DESC")
    Page<AnomalyDetection> findByRoomIdAndOptionalSeverity(@Param("roomId") String roomId, @Param("severity") String severity, Pageable pageable);

    long countByRoomFloorBuildingIdAndAnomalyDetectedTrue(String buildingId);

    long countByRoomFloorBuildingIdAndSeverity(String buildingId, String severity);
}
