package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.Incident;
import com.smartbuilding.emergency.model.IncidentCategory;
import com.smartbuilding.emergency.model.IncidentSeverity;
import com.smartbuilding.emergency.model.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, String> {

    Optional<Incident> findByReferenceNumber(String referenceNumber);

    List<Incident> findByBuildingIdOrderByDetectedAtDesc(String buildingId);

    List<Incident> findByBuildingIdAndStatusInOrderByDetectedAtDesc(String buildingId, List<IncidentStatus> statuses);

    List<Incident> findByStatusInOrderByDetectedAtDesc(List<IncidentStatus> statuses);

    @Query("SELECT i FROM Incident i WHERE (:buildingId IS NULL OR i.building.id = :buildingId) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:severity IS NULL OR i.severity = :severity) " +
           "AND (:category IS NULL OR i.category = :category) " +
           "ORDER BY i.detectedAt DESC")
    Page<Incident> findFilteredIncidents(
            @Param("buildingId") String buildingId,
            @Param("status") IncidentStatus status,
            @Param("severity") IncidentSeverity severity,
            @Param("category") IncidentCategory category,
            Pageable pageable
    );

    @Query("SELECT i FROM Incident i WHERE i.room.id = :roomId AND i.category = :category " +
           "AND i.status NOT IN ('RESOLVED', 'CLOSED') AND i.detectedAt >= :sinceTime")
    List<Incident> findActiveIncidentForRoomAndCategory(
            @Param("roomId") String roomId,
            @Param("category") IncidentCategory category,
            @Param("sinceTime") Instant sinceTime
    );

    long countByBuildingIdAndStatusIn(String buildingId, List<IncidentStatus> statuses);

    long countByBuildingIdAndSeverity(String buildingId, IncidentSeverity severity);
}
