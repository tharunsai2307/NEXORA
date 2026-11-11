package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.EvacuationSession;
import com.smartbuilding.emergency.model.EvacuationSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvacuationSessionRepository extends JpaRepository<EvacuationSession, String> {
    List<EvacuationSession> findByBuildingIdOrderByCreatedAtDesc(String buildingId);
    Optional<EvacuationSession> findFirstByBuildingIdAndSessionStatusInOrderByCreatedAtDesc(String buildingId, List<EvacuationSessionStatus> statuses);
    Optional<EvacuationSession> findFirstByIncidentIdOrderByCreatedAtDesc(String incidentId);
}
