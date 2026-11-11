package com.smartbuilding.simulation.service;

import com.smartbuilding.simulation.dto.*;
import com.smartbuilding.simulation.model.ScenarioType;

import java.util.List;
import java.util.Map;

public interface SimulationEngineService {

    SimulationSessionResponse startSimulation(StartSimulationRequest request, String startedBy);

    SimulationSessionResponse pauseSimulation(String sessionId);

    SimulationSessionResponse resumeSimulation(String sessionId);

    SimulationSessionResponse stopSimulation(String sessionId);

    SimulationSessionResponse getActiveSession(String buildingId);

    List<SimulationSessionResponse> getAllActiveSessions();

    List<SimulationSessionResponse> getSessionHistory();

    List<Map<String, Object>> getAvailableScenarios();

    SimulationScenarioResponse activateScenario(String sessionId, ScenarioRequest request);

    SimulationScenarioResponse stopScenario(String scenarioId);

    boolean isSimulationRunning(String buildingId);
}

