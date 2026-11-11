package com.smartbuilding.simulation.service;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.emergency.model.IncidentCategory;
import com.smartbuilding.emergency.model.IncidentSeverity;
import com.smartbuilding.emergency.model.IncidentSource;
import com.smartbuilding.emergency.service.IncidentService;
import com.smartbuilding.sensor.dto.SensorReadingResponse;
import com.smartbuilding.sensor.model.*;
import com.smartbuilding.sensor.repository.SensorReadingRepository;
import com.smartbuilding.sensor.repository.SensorRepository;
import com.smartbuilding.sensor.service.SensorRealtimeBroadcaster;
import com.smartbuilding.simulation.dto.ScenarioRequest;
import com.smartbuilding.simulation.dto.SimulationScenarioResponse;
import com.smartbuilding.simulation.dto.SimulationSessionResponse;
import com.smartbuilding.simulation.dto.StartSimulationRequest;
import com.smartbuilding.simulation.model.ScenarioStatus;
import com.smartbuilding.simulation.model.ScenarioType;
import com.smartbuilding.simulation.model.SessionStatus;
import com.smartbuilding.simulation.model.SimulationScenario;
import com.smartbuilding.simulation.model.SimulationSession;
import com.smartbuilding.simulation.repository.SimulationScenarioRepository;
import com.smartbuilding.simulation.repository.SimulationSessionRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class SimulationEngineServiceImpl implements SimulationEngineService {

    private static final Logger logger = LoggerFactory.getLogger(SimulationEngineServiceImpl.class);

    private final SimulationSessionRepository sessionRepository;
    private final SimulationScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final SensorRealtimeBroadcaster broadcaster;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private IncidentService incidentService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4, new ThreadFactory() {
        private int count = 1;
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "nexora-simulation-worker-" + count++);
            t.setDaemon(true);
            return t;
        }
    });

    private final ConcurrentHashMap<String, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Random> sessionRandoms = new ConcurrentHashMap<>();

    public SimulationEngineServiceImpl(
            SimulationSessionRepository sessionRepository,
            SimulationScenarioRepository scenarioRepository,
            SensorRepository sensorRepository,
            SensorReadingRepository readingRepository,
            BuildingRepository buildingRepository,
            RoomRepository roomRepository,
            SensorRealtimeBroadcaster broadcaster) {
        this.sessionRepository = sessionRepository;
        this.scenarioRepository = scenarioRepository;
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.broadcaster = broadcaster;
    }

    @Override
    @Transactional
    public SimulationSessionResponse startSimulation(StartSimulationRequest request, String startedBy) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + request.getBuildingId()));

        // Check if an active session already exists for this building
        Optional<SimulationSession> existing = sessionRepository.findByBuildingIdAndStatus(building.getId(), SessionStatus.RUNNING);
        if (existing.isPresent()) {
            throw ServiceException.conflict("An active simulation session already exists for building: " + building.getName());
        }

        String sessionName = request.getSessionName();
        if (sessionName == null || sessionName.trim().isEmpty()) {
            sessionName = "SimSession-" + building.getBuildingCode() + "-" + System.currentTimeMillis() % 10000;
        }

        int interval = request.getSimulationIntervalSeconds() != null && request.getSimulationIntervalSeconds() >= 1
                ? Math.min(60, request.getSimulationIntervalSeconds()) : 5;

        ScenarioType scenarioType = request.getScenarioType() != null ? request.getScenarioType() : ScenarioType.NORMAL;

        SimulationSession session = new SimulationSession(building, sessionName, startedBy, scenarioType, interval);
        SimulationSession savedSession = sessionRepository.save(session);

        // Create default scenario
        SimulationScenario scenario = new SimulationScenario(
                savedSession,
                "Initial " + scenarioType.name() + " Scenario",
                scenarioType,
                "{}",
                300
        );
        scenarioRepository.save(scenario);

        // Schedule periodic task
        scheduleSessionTask(savedSession);

        int activeSensors = sensorRepository.findByBuildingIdAndStatus(building.getId(), SensorStatus.ACTIVE).size();
        SimulationSessionResponse response = SimulationSessionResponse.fromEntity(savedSession, activeSensors);
        broadcaster.broadcastSessionStatus(response);

        logger.info("Started simulation session [{}] for building {} (Interval: {}s)", savedSession.getId(), building.getName(), interval);
        return response;
    }

    @Override
    @Transactional
    public SimulationSessionResponse pauseSimulation(String sessionId) {
        SimulationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Simulation session not found with ID: " + sessionId));

        if (session.getStatus() != SessionStatus.RUNNING) {
            throw ServiceException.badRequest("Session is not in RUNNING state (Current state: " + session.getStatus() + ")");
        }

        cancelScheduledTask(sessionId);
        session.setStatus(SessionStatus.PAUSED);
        session.setPausedAt(Instant.now());
        SimulationSession saved = sessionRepository.save(session);

        int activeSensors = sensorRepository.findByBuildingIdAndStatus(session.getBuilding().getId(), SensorStatus.ACTIVE).size();
        SimulationSessionResponse response = SimulationSessionResponse.fromEntity(saved, activeSensors);
        broadcaster.broadcastSessionStatus(response);

        logger.info("Paused simulation session [{}]", sessionId);
        return response;
    }

    @Override
    @Transactional
    public SimulationSessionResponse resumeSimulation(String sessionId) {
        SimulationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Simulation session not found with ID: " + sessionId));

        if (session.getStatus() != SessionStatus.PAUSED) {
            throw ServiceException.badRequest("Session is not in PAUSED state (Current state: " + session.getStatus() + ")");
        }

        session.setStatus(SessionStatus.RUNNING);
        session.setPausedAt(null);
        SimulationSession saved = sessionRepository.save(session);

        scheduleSessionTask(saved);

        int activeSensors = sensorRepository.findByBuildingIdAndStatus(session.getBuilding().getId(), SensorStatus.ACTIVE).size();
        SimulationSessionResponse response = SimulationSessionResponse.fromEntity(saved, activeSensors);
        broadcaster.broadcastSessionStatus(response);

        logger.info("Resumed simulation session [{}]", sessionId);
        return response;
    }

    @Override
    @Transactional
    public SimulationSessionResponse stopSimulation(String sessionId) {
        SimulationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Simulation session not found with ID: " + sessionId));

        cancelScheduledTask(sessionId);

        session.setStatus(SessionStatus.STOPPED);
        session.setStoppedAt(Instant.now());
        SimulationSession saved = sessionRepository.save(session);

        // Mark active scenario completed
        scenarioRepository.findBySessionIdAndStatus(sessionId, ScenarioStatus.ACTIVE)
                .ifPresent(sc -> {
                    sc.setStatus(ScenarioStatus.COMPLETED);
                    sc.setEndedAt(Instant.now());
                    scenarioRepository.save(sc);
                });

        int activeSensors = sensorRepository.findByBuildingIdAndStatus(session.getBuilding().getId(), SensorStatus.ACTIVE).size();
        SimulationSessionResponse response = SimulationSessionResponse.fromEntity(saved, activeSensors);
        broadcaster.broadcastSessionStatus(response);

        logger.info("Stopped simulation session [{}]", sessionId);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationSessionResponse getActiveSession(String buildingId) {
        Optional<SimulationSession> sessionOpt = sessionRepository.findByBuildingIdAndStatus(buildingId, SessionStatus.RUNNING);
        if (sessionOpt.isEmpty()) {
            sessionOpt = sessionRepository.findByBuildingIdAndStatus(buildingId, SessionStatus.PAUSED);
        }
        if (sessionOpt.isEmpty()) {
            return null;
        }
        SimulationSession s = sessionOpt.get();
        int activeSensors = sensorRepository.findByBuildingIdAndStatus(s.getBuilding().getId(), SensorStatus.ACTIVE).size();
        return SimulationSessionResponse.fromEntity(s, activeSensors);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimulationSessionResponse> getAllActiveSessions() {
        return sessionRepository.findByStatus(SessionStatus.RUNNING).stream()
                .map(s -> {
                    int count = sensorRepository.findByBuildingIdAndStatus(s.getBuilding().getId(), SensorStatus.ACTIVE).size();
                    return SimulationSessionResponse.fromEntity(s, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimulationSessionResponse> getSessionHistory() {
        return sessionRepository.findTop20ByOrderByStartedAtDesc().stream()
                .map(s -> {
                    int count = sensorRepository.findByBuildingIdAndStatus(s.getBuilding().getId(), SensorStatus.ACTIVE).size();
                    return SimulationSessionResponse.fromEntity(s, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAvailableScenarios() {
        return List.of(
                Map.of(
                        "type", ScenarioType.NORMAL.name(),
                        "name", "Normal Building Operation",
                        "description", "Simulates nominal day-to-day conditions with gradual Brownian fluctuations, healthy AQI, near-zero smoke, and normal occupancy distributions.",
                        "severity", "LOW"
                ),
                Map.of(
                        "type", ScenarioType.TEMPERATURE_ANOMALY.name(),
                        "name", "Thermal Spike Anomaly",
                        "description", "Causes progressive temperature ramp (up to 45°C) across selected zones, simulating HVAC failure or overheating servers.",
                        "severity", "MEDIUM"
                ),
                Map.of(
                        "type", ScenarioType.SMOKE_EVENT.name(),
                        "name", "Smoke & Fire Incipient Event",
                        "description", "Simulates rapid optical smoke particle surge (up to 15+ ppm) with correlated ambient temperature increase to test early hazard detection.",
                        "severity", "CRITICAL"
                ),
                Map.of(
                        "type", ScenarioType.POOR_AIR_QUALITY.name(),
                        "name", "Air Quality Degradation (AQI)",
                        "description", "Deteriorates indoor air quality index (AQI climbing to 200–400+), mimicking toxic fumes, ventilation loss, or dust ingress.",
                        "severity", "HIGH"
                ),
                Map.of(
                        "type", ScenarioType.HIGH_OCCUPANCY.name(),
                        "name", "Peak Crowd & High Occupancy",
                        "description", "Progressively fills rooms towards 90%–100% capacity limit without exceeding physical constraints, testing crowd limits.",
                        "severity", "MEDIUM"
                ),
                Map.of(
                        "type", ScenarioType.CUSTOM.name(),
                        "name", "Custom Scenario Configuration",
                        "description", "Allows selective targeting of specific rooms, floor zones, custom offset ranges, and duration.",
                        "severity", "VARIABLE"
                )
        );
    }

    @Override
    @Transactional
    public SimulationScenarioResponse activateScenario(String sessionId, ScenarioRequest request) {
        SimulationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Simulation session not found with ID: " + sessionId));

        // Complete any current active scenario for this session
        scenarioRepository.findBySessionIdAndStatus(sessionId, ScenarioStatus.ACTIVE)
                .ifPresent(sc -> {
                    sc.setStatus(ScenarioStatus.COMPLETED);
                    sc.setEndedAt(Instant.now());
                    scenarioRepository.save(sc);
                });

        session.setScenarioType(request.getScenarioType());
        sessionRepository.save(session);

        String name = request.getScenarioName() != null && !request.getScenarioName().trim().isEmpty()
                ? request.getScenarioName().trim() : request.getScenarioType().name() + " Scenario";

        SimulationScenario newScenario = new SimulationScenario(
                session,
                name,
                request.getScenarioType(),
                request.getTargetConfiguration(),
                request.getDurationSeconds() != null ? request.getDurationSeconds() : 300
        );
        SimulationScenario saved = scenarioRepository.save(newScenario);

        int activeSensors = sensorRepository.findByBuildingIdAndStatus(session.getBuilding().getId(), SensorStatus.ACTIVE).size();
        broadcaster.broadcastSessionStatus(SimulationSessionResponse.fromEntity(session, activeSensors));

        logger.info("Activated scenario [{}] on session [{}]", request.getScenarioType(), sessionId);
        return SimulationScenarioResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public SimulationScenarioResponse stopScenario(String scenarioId) {
        SimulationScenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> ServiceException.notFound("Scenario not found with ID: " + scenarioId));

        scenario.setStatus(ScenarioStatus.COMPLETED);
        scenario.setEndedAt(Instant.now());
        SimulationScenario saved = scenarioRepository.save(scenario);

        // Revert session back to NORMAL scenario
        SimulationSession session = scenario.getSession();
        if (session != null && session.getStatus() == SessionStatus.RUNNING) {
            session.setScenarioType(ScenarioType.NORMAL);
            sessionRepository.save(session);

            SimulationScenario normalSc = new SimulationScenario(
                    session,
                    "Reverted Normal Operation",
                    ScenarioType.NORMAL,
                    "{}",
                    300
            );
            scenarioRepository.save(normalSc);
        }

        logger.info("Stopped scenario [{}] and reverted to NORMAL", scenarioId);
        return SimulationScenarioResponse.fromEntity(saved);
    }

    @Override
    public boolean isSimulationRunning(String buildingId) {
        return sessionRepository.findByBuildingIdAndStatus(buildingId, SessionStatus.RUNNING).isPresent();
    }

    // Scheduling and Engine Tick Execution

    private void scheduleSessionTask(SimulationSession session) {
        cancelScheduledTask(session.getId());

        int interval = session.getSimulationIntervalSeconds() > 0 ? session.getSimulationIntervalSeconds() : 5;
        sessionRandoms.computeIfAbsent(session.getId(), k -> new Random());

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> executeSimulationTick(session.getId()),
                1,
                interval,
                TimeUnit.SECONDS
        );

        activeTasks.put(session.getId(), future);
        logger.debug("Scheduled simulation tick for session {} every {}s", session.getId(), interval);
    }

    private void cancelScheduledTask(String sessionId) {
        ScheduledFuture<?> future = activeTasks.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }
        sessionRandoms.remove(sessionId);
    }

    /**
     * Executes one tick of virtual sensor generation for the given simulation session.
     */
    @Transactional
    public void executeSimulationTick(String sessionId) {
        try {
            SimulationSession session = sessionRepository.findById(sessionId).orElse(null);
            if (session == null || session.getStatus() != SessionStatus.RUNNING) {
                cancelScheduledTask(sessionId);
                return;
            }

            String buildingId = session.getBuilding().getId();
            List<Sensor> sensors = sensorRepository.findByBuildingIdAndStatus(buildingId, SensorStatus.ACTIVE);
            if (sensors.isEmpty()) {
                return;
            }

            ScenarioType scenarioType = session.getScenarioType();
            Random rand = sessionRandoms.computeIfAbsent(sessionId, k -> new Random());

            List<SensorReading> batchReadings = new ArrayList<>();
            List<SensorReadingResponse> broadcastResponses = new ArrayList<>();

            for (Sensor sensor : sensors) {
                try {
                    double currentVal = sensor.getLastReadingValue() != null
                            ? sensor.getLastReadingValue() : getDefaultBaseline(sensor.getSensorType());

                    double nextVal = computeNextReading(sensor, currentVal, scenarioType, rand);
                    nextVal = Math.round(nextVal * 100.0) / 100.0;

                    sensor.setLastReadingValue(nextVal);
                    sensor.setLastReadingAt(Instant.now());

                    SensorReading reading = new SensorReading(sensor, nextVal, ReadingSource.SIMULATED, sessionId);
                    batchReadings.add(reading);
                    broadcastResponses.add(SensorReadingResponse.fromEntity(reading));

                    // If Occupancy Sensor, synchronize Room occupancy
                    if (sensor.getSensorType() == SensorType.OCCUPANCY && sensor.getRoom() != null) {
                        Room room = sensor.getRoom();
                        int newOcc = (int) Math.round(nextVal);
                        room.setCurrentOccupancy(newOcc);
                        roomRepository.save(room);

                        broadcaster.broadcastRoomOccupancy(buildingId, room.getId(), newOcc, room.getMaximumCapacity());
                    }

                    // Check for emergency thresholds during simulation
                    if (incidentService != null && sensor.getRoom() != null) {
                        Room room = sensor.getRoom();
                        if (sensor.getSensorType() == SensorType.SMOKE && nextVal > 60.0) {
                            incidentService.processSensorOrAiDetection(
                                    buildingId, room.getId(), IncidentCategory.SMOKE,
                                    nextVal > 75.0 ? IncidentSeverity.CRITICAL : IncidentSeverity.HIGH,
                                    IncidentSource.SENSOR,
                                    "Simulated Smoke Alert: " + Math.round(nextVal) + " ppm",
                                    "Sensor " + sensor.getSensorName() + " triggered emergency threshold during simulation."
                            );
                        } else if (sensor.getSensorType() == SensorType.TEMPERATURE && nextVal > 46.0) {
                            incidentService.processSensorOrAiDetection(
                                    buildingId, room.getId(), IncidentCategory.HIGH_TEMPERATURE,
                                    IncidentSeverity.HIGH,
                                    IncidentSource.SENSOR,
                                    "Simulated Heat Warning: " + Math.round(nextVal) + "°C",
                                    "Sensor " + sensor.getSensorName() + " detected dangerous temperature during simulation."
                            );
                        }
                    }

                } catch (Exception ex) {
                    logger.warn("Error simulating sensor {}: {}", sensor.getId(), ex.getMessage());
                }
            }

            // Save sensor updates and batch readings
            sensorRepository.saveAll(sensors);
            readingRepository.saveAll(batchReadings);

            // Broadcast real-time sensor updates over WebSocket
            broadcaster.broadcastBatchReadings(buildingId, broadcastResponses);

        } catch (Exception e) {
            logger.error("Simulation tick failed for session {}: {}", sessionId, e.getMessage());
        }
    }

    private double computeNextReading(Sensor sensor, double current, ScenarioType scenario, Random rand) {
        SensorType type = sensor.getSensorType();

        switch (type) {
            case TEMPERATURE:
                if (scenario == ScenarioType.TEMPERATURE_ANOMALY || scenario == ScenarioType.SMOKE_EVENT) {
                    // Thermal surge: climbs by 0.6 - 1.2 °C up to 48°C
                    return Math.min(48.0, current + 0.6 + rand.nextDouble() * 0.6);
                }
                // Normal fluctuation: mean reversion towards 22.5°C
                double tempDrift = (22.5 - current) * 0.08 + (rand.nextDouble() - 0.5) * 0.4;
                return Math.max(18.0, Math.min(28.0, current + tempDrift));

            case HUMIDITY:
                if (scenario == ScenarioType.TEMPERATURE_ANOMALY || scenario == ScenarioType.SMOKE_EVENT) {
                    // Humidity drops in high heat
                    return Math.max(15.0, current - 0.8 - rand.nextDouble() * 0.4);
                }
                // Normal humidity: mean reversion towards 46%
                double humDrift = (46.0 - current) * 0.08 + (rand.nextDouble() - 0.5) * 0.8;
                return Math.max(30.0, Math.min(65.0, current + humDrift));

            case SMOKE:
                if (scenario == ScenarioType.SMOKE_EVENT) {
                    // Rapid smoke surge: +0.6 to +2.0 ppm per tick up to 25.0 ppm
                    return Math.min(25.0, current + 0.6 + rand.nextDouble() * 1.4);
                }
                // Normal smoke: stays near zero (0.00 - 0.05 ppm)
                double normalSmoke = Math.max(0.0, current + (rand.nextDouble() - 0.5) * 0.01);
                return Math.min(0.08, normalSmoke);

            case AIR_QUALITY:
                if (scenario == ScenarioType.POOR_AIR_QUALITY || scenario == ScenarioType.SMOKE_EVENT) {
                    // Rapid AQI deterioration up to 450
                    return Math.min(480.0, current + 8.0 + rand.nextDouble() * 15.0);
                }
                // Normal AQI: mean reversion towards 42
                double aqiDrift = (42.0 - current) * 0.1 + (rand.nextDouble() - 0.5) * 4.0;
                return Math.max(15.0, Math.min(80.0, current + aqiDrift));

            case OCCUPANCY:
                int maxCap = sensor.getRoom() != null ? sensor.getRoom().getMaximumCapacity() : 50;
                if (scenario == ScenarioType.HIGH_OCCUPANCY) {
                    // Surge towards 95% of room capacity
                    int target = (int) Math.round(maxCap * 0.95);
                    if (current < target) {
                        return Math.min(maxCap, current + 1 + rand.nextInt(3));
                    }
                    return Math.min(maxCap, current + (rand.nextBoolean() ? 1 : -1));
                }
                // Normal occupancy: random step within 0 and capacity
                int delta = rand.nextInt(5) - 2; // -2 to +2
                double nextOcc = Math.max(0, Math.min(maxCap, current + delta));
                return nextOcc;

            default:
                return current;
        }
    }

    private double getDefaultBaseline(SensorType type) {
        return switch (type) {
            case TEMPERATURE -> 22.5;
            case HUMIDITY -> 45.0;
            case SMOKE -> 0.01;
            case AIR_QUALITY -> 40.0;
            case OCCUPANCY -> 0.0;
        };
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Simulation Engine thread pool...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

