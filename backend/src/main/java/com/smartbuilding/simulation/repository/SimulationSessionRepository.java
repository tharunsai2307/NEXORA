package com.smartbuilding.simulation.repository;

import com.smartbuilding.simulation.model.SessionStatus;
import com.smartbuilding.simulation.model.SimulationSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationSessionRepository extends JpaRepository<SimulationSession, String> {

    Optional<SimulationSession> findByBuildingIdAndStatus(String buildingId, SessionStatus status);

    List<SimulationSession> findByStatus(SessionStatus status);

    List<SimulationSession> findByBuildingId(String buildingId);

    Page<SimulationSession> findAllByOrderByStartedAtDesc(Pageable pageable);

    List<SimulationSession> findTop20ByOrderByStartedAtDesc();
}
