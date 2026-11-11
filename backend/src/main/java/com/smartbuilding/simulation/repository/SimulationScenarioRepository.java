package com.smartbuilding.simulation.repository;

import com.smartbuilding.simulation.model.ScenarioStatus;
import com.smartbuilding.simulation.model.SimulationScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationScenarioRepository extends JpaRepository<SimulationScenario, String> {

    Optional<SimulationScenario> findBySessionIdAndStatus(String sessionId, ScenarioStatus status);

    List<SimulationScenario> findBySessionIdOrderByStartedAtDesc(String sessionId);
}
