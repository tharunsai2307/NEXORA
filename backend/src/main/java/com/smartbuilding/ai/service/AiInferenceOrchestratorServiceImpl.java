package com.smartbuilding.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.ai.dto.*;
import com.smartbuilding.ai.model.AiModelMetadata;
import com.smartbuilding.ai.model.AiPrediction;
import com.smartbuilding.ai.model.AnomalyDetection;
import com.smartbuilding.ai.model.RiskAssessment;
import com.smartbuilding.ai.repository.AiModelMetadataRepository;
import com.smartbuilding.ai.repository.AiPredictionRepository;
import com.smartbuilding.ai.repository.AnomalyDetectionRepository;
import com.smartbuilding.ai.repository.RiskAssessmentRepository;
import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.emergency.model.IncidentCategory;
import com.smartbuilding.emergency.model.IncidentSeverity;
import com.smartbuilding.emergency.model.IncidentSource;
import com.smartbuilding.emergency.service.IncidentService;
import com.smartbuilding.sensor.model.Sensor;
import com.smartbuilding.sensor.model.SensorReading;
import com.smartbuilding.sensor.model.SensorType;
import com.smartbuilding.sensor.repository.SensorReadingRepository;
import com.smartbuilding.sensor.repository.SensorRepository;
import com.smartbuilding.sensor.service.SensorRealtimeBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class AiInferenceOrchestratorServiceImpl implements AiInferenceOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(AiInferenceOrchestratorServiceImpl.class);

    private final AiServiceClient aiServiceClient;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final AnomalyDetectionRepository anomalyRepository;
    private final AiPredictionRepository predictionRepository;
    private final RiskAssessmentRepository riskRepository;
    private final AiModelMetadataRepository modelMetadataRepository;
    private final SensorRealtimeBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private IncidentService incidentService;

    public AiInferenceOrchestratorServiceImpl(
            AiServiceClient aiServiceClient,
            RoomRepository roomRepository,
            BuildingRepository buildingRepository,
            SensorRepository sensorRepository,
            SensorReadingRepository readingRepository,
            AnomalyDetectionRepository anomalyRepository,
            AiPredictionRepository predictionRepository,
            RiskAssessmentRepository riskRepository,
            AiModelMetadataRepository modelMetadataRepository,
            SensorRealtimeBroadcaster broadcaster,
            ObjectMapper objectMapper) {
        this.aiServiceClient = aiServiceClient;
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.anomalyRepository = anomalyRepository;
        this.predictionRepository = predictionRepository;
        this.riskRepository = riskRepository;
        this.modelMetadataRepository = modelMetadataRepository;
        this.broadcaster = broadcaster;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public RoomAiOverviewResponse analyzeRoom(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));

        String buildingId = room.getFloor().getBuilding().getId();
        int capacity = Math.max(1, room.getMaximumCapacity());

        // 1. Gather latest readings from sensors
        List<Sensor> sensors = sensorRepository.findByRoomId(roomId);
        Double temperature = null;
        Double humidity = null;
        Double smoke = null;
        Double airQuality = null;
        Integer occupancy = room.getCurrentOccupancy();
        Double tempAvg = null;
        Double smokeAvg = null;
        List<Integer> occHistory = new ArrayList<>();

        for (Sensor sensor : sensors) {
            List<SensorReading> recents = readingRepository.findTop50BySensorIdOrderByRecordedAtDesc(sensor.getId());
            Double latestVal = (recents != null && !recents.isEmpty()) ? recents.get(0).getReadingValue() : null;

            if (sensor.getSensorType() == SensorType.TEMPERATURE) {
                temperature = latestVal;
                if (recents != null && !recents.isEmpty()) {
                    tempAvg = recents.stream().limit(10).mapToDouble(SensorReading::getReadingValue).average().orElse(22.0);
                }
            } else if (sensor.getSensorType() == SensorType.HUMIDITY) {
                humidity = latestVal;
            } else if (sensor.getSensorType() == SensorType.SMOKE) {
                smoke = latestVal;
                if (recents != null && !recents.isEmpty()) {
                    smokeAvg = recents.stream().limit(10).mapToDouble(SensorReading::getReadingValue).average().orElse(0.4);
                }
            } else if (sensor.getSensorType() == SensorType.AIR_QUALITY) {
                airQuality = latestVal;
            } else if (sensor.getSensorType() == SensorType.OCCUPANCY) {
                if (latestVal != null) {
                    occupancy = (int) Math.round(latestVal);
                }
                if (recents != null) {
                    for (SensorReading r : recents) {
                        occHistory.add((int) Math.round(r.getReadingValue()));
                        if (occHistory.size() >= 10) break;
                    }
                }
            }
        }

        // Apply defaults if no sensors yet
        if (temperature == null) temperature = 22.0;
        if (humidity == null) humidity = 45.0;
        if (smoke == null) smoke = 0.4;
        if (airQuality == null) airQuality = 40.0;
        if (occupancy == null) occupancy = 0;

        // 2. Anomaly Detection
        AnomalyInferenceResponse anomRes;
        try {
            AnomalyInferenceRequest anomReq = new AnomalyInferenceRequest(
                    roomId, temperature, humidity, smoke, airQuality, occupancy,
                    capacity, tempAvg, smokeAvg, Instant.now().toString()
            );
            anomRes = aiServiceClient.detectAnomaly(anomReq);
        } catch (Exception ex) {
            logger.warn("AI anomaly service unavailable for room {}. Applying local safety heuristics: {}", roomId, ex.getMessage());
            anomRes = calculateFallbackAnomaly(roomId, temperature, smoke, airQuality, occupancy, capacity);
        }

        // 3. Occupancy Prediction
        OccupancyPredictionResponse occRes;
        try {
            int currentHour = LocalTime.now(ZoneOffset.UTC).getHour();
            int currentDow = 1; // Mon-Sun index
            OccupancyPredictionRequest occReq = new OccupancyPredictionRequest(
                    roomId, occupancy, room.getRoomType().name(), capacity,
                    occHistory, currentHour, currentDow, 15
            );
            occRes = aiServiceClient.predictOccupancy(occReq);
        } catch (Exception ex) {
            logger.warn("AI occupancy service unavailable for room {}. Applying local forecast fallback: {}", roomId, ex.getMessage());
            occRes = calculateFallbackOccupancy(roomId, occupancy, capacity);
        }

        // 4. Risk Assessment
        RiskAssessmentResponse riskRes;
        try {
            RiskAssessmentRequest riskReq = new RiskAssessmentRequest(
                    roomId, temperature, humidity, smoke, airQuality, occupancy,
                    capacity, anomRes.anomalyDetected(), anomRes.anomalyScore()
            );
            riskRes = aiServiceClient.assessRisk(riskReq);
        } catch (Exception ex) {
            logger.warn("AI risk assessment service unavailable for room {}. Applying local risk formula: {}", roomId, ex.getMessage());
            riskRes = calculateFallbackRisk(roomId, temperature, smoke, airQuality, occupancy, capacity, anomRes);
        }

        // 5. Persist Anomaly Detection
        String featuresJson = "{}";
        try {
            featuresJson = objectMapper.writeValueAsString(anomRes.contributingFeatures());
        } catch (Exception ignored) {}

        AnomalyDetection anomaly = new AnomalyDetection(
                UUID.randomUUID().toString(),
                room,
                anomRes.anomalyScore(),
                anomRes.severity(),
                anomRes.anomalyDetected(),
                featuresJson,
                anomRes.explanation(),
                anomRes.modelVersion(),
                Instant.now()
        );
        anomalyRepository.save(anomaly);

        // 6. Persist Occupancy Prediction
        AiPrediction prediction = new AiPrediction(
                UUID.randomUUID().toString(),
                room,
                "OCCUPANCY_15MIN",
                (double) occRes.predictedOccupancy(),
                occRes.predictionHorizonMinutes(),
                occRes.confidenceScore(),
                occRes.modelVersion(),
                Instant.now().plusSeconds(occRes.predictionHorizonMinutes() * 60L)
        );
        predictionRepository.save(prediction);

        // 7. Persist Risk Assessment
        String factorsJson = "{}";
        try {
            factorsJson = objectMapper.writeValueAsString(riskRes.contributingFactors());
        } catch (Exception ignored) {}

        RiskAssessment risk = new RiskAssessment(
                UUID.randomUUID().toString(),
                room,
                riskRes.overallRiskScore(),
                riskRes.riskLevel(),
                riskRes.temperatureScore(),
                riskRes.smokeScore(),
                riskRes.airQualityScore(),
                riskRes.occupancyScore(),
                riskRes.anomalyScore(),
                factorsJson,
                riskRes.explanation(),
                Instant.now()
        );
        riskRepository.save(risk);

        // 8. WebSocket Broadcasts
        broadcaster.broadcastAiAnomaly(buildingId, Map.of(
                "roomId", roomId,
                "buildingId", buildingId,
                "severity", anomRes.severity(),
                "anomalyDetected", anomRes.anomalyDetected(),
                "anomalyScore", anomRes.anomalyScore(),
                "explanation", anomRes.explanation(),
                "timestamp", Instant.now().toString()
        ));

        broadcaster.broadcastAiPrediction(buildingId, Map.of(
                "roomId", roomId,
                "buildingId", buildingId,
                "currentOccupancy", occupancy,
                "predictedOccupancy", occRes.predictedOccupancy(),
                "predictedUtilizationPct", occRes.predictedUtilizationPct(),
                "horizonMinutes", occRes.predictionHorizonMinutes(),
                "timestamp", Instant.now().toString()
        ));

        broadcaster.broadcastAiRisk(buildingId, Map.of(
                "roomId", roomId,
                "buildingId", buildingId,
                "riskScore", riskRes.overallRiskScore(),
                "riskLevel", riskRes.riskLevel(),
                "criticalOverride", riskRes.criticalOverride(),
                "explanation", riskRes.explanation(),
                "timestamp", Instant.now().toString()
        ));

        // Auto-generate Incident if AI assesses CRITICAL risk
        if (incidentService != null && ("CRITICAL".equalsIgnoreCase(riskRes.riskLevel()) || (riskRes.overallRiskScore() != null && riskRes.overallRiskScore() >= 75.0))) {
            IncidentCategory cat = (smoke != null && smoke > 50.0) ? IncidentCategory.SMOKE
                    : ((temperature != null && temperature > 45.0) ? IncidentCategory.HIGH_TEMPERATURE : IncidentCategory.FIRE);
            incidentService.processSensorOrAiDetection(
                    buildingId,
                    roomId,
                    cat,
                    IncidentSeverity.CRITICAL,
                    IncidentSource.AI,
                    "AI Emergency Alert: " + cat + " in " + room.getRoomName(),
                    riskRes.explanation()
            );
        }

        // 9. Build response
        Map<String, Object> snapshots = Map.of(
                "temperature", temperature,
                "humidity", humidity,
                "smoke", smoke,
                "airQuality", airQuality,
                "occupancy", occupancy
        );

        double curUtil = Math.round(((double) occupancy / capacity) * 1000.0) / 10.0;

        return new RoomAiOverviewResponse(
                roomId,
                room.getRoomName(),
                room.getRoomCode(),
                room.getFloor().getId(),
                room.getFloor().getFloorNumber(),
                buildingId,
                occupancy,
                capacity,
                curUtil,
                riskRes.overallRiskScore(),
                riskRes.riskLevel(),
                (double) occRes.predictedOccupancy(),
                occRes.predictedUtilizationPct(),
                anomRes.anomalyDetected(),
                anomRes.severity(),
                anomRes.anomalyScore(),
                anomRes.explanation(),
                snapshots,
                Instant.now()
        );
    }

    @Override
    @Transactional
    public BuildingAiSummaryResponse analyzeBuilding(String buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        List<Room> rooms = roomRepository.findByFloorBuildingId(buildingId);
        for (Room r : rooms) {
            try {
                analyzeRoom(r.getId());
            } catch (Exception e) {
                logger.error("Failed to analyze room {}: {}", r.getId(), e.getMessage());
            }
        }
        return getBuildingAiSummary(buildingId);
    }

    @Override
    public RoomAiOverviewResponse getRoomAiOverview(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));

        int capacity = Math.max(1, room.getMaximumCapacity());
        int occupancy = room.getCurrentOccupancy();
        double curUtil = Math.round(((double) occupancy / capacity) * 1000.0) / 10.0;

        Optional<RiskAssessment> latestRisk = riskRepository.findFirstByRoomIdOrderByAssessedAtDesc(roomId);
        Optional<AnomalyDetection> latestAnom = anomalyRepository.findFirstByRoomIdOrderByDetectedAtDesc(roomId);
        Optional<AiPrediction> latestPred = predictionRepository.findFirstByRoomIdOrderByPredictionTimestampDesc(roomId);

        Double riskScore = latestRisk.map(RiskAssessment::getRiskScore).orElse(10.0);
        String riskLevel = latestRisk.map(RiskAssessment::getRiskLevel).orElse("NORMAL");

        Double predVal = latestPred.map(AiPrediction::getPredictedValue).orElse((double) occupancy);
        Double predUtil = Math.round((predVal / capacity) * 1000.0) / 10.0;

        Boolean anomDet = latestAnom.map(AnomalyDetection::getAnomalyDetected).orElse(false);
        String anomSev = latestAnom.map(AnomalyDetection::getSeverity).orElse("NORMAL");
        Double anomScr = latestAnom.map(AnomalyDetection::getAnomalyScore).orElse(0.1);
        String explanation = latestAnom.map(AnomalyDetection::getExplanation).orElse("Nominal operating conditions.");

        return new RoomAiOverviewResponse(
                roomId,
                room.getRoomName(),
                room.getRoomCode(),
                room.getFloor().getId(),
                room.getFloor().getFloorNumber(),
                room.getFloor().getBuilding().getId(),
                occupancy,
                capacity,
                curUtil,
                riskScore,
                riskLevel,
                predVal,
                predUtil,
                anomDet,
                anomSev,
                anomScr,
                explanation,
                Map.of(),
                latestRisk.map(RiskAssessment::getAssessedAt).orElse(Instant.now())
        );
    }

    @Override
    public BuildingAiSummaryResponse getBuildingAiSummary(String buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        List<Room> rooms = roomRepository.findByFloorBuildingId(buildingId);
        int totalRooms = rooms.size();

        Double avgRisk = riskRepository.getAverageRiskScoreByBuildingId(buildingId);
        if (avgRisk == null) avgRisk = 12.0;
        avgRisk = Math.round(avgRisk * 10.0) / 10.0;

        long criticalRooms = riskRepository.countByRoomFloorBuildingIdAndRiskLevel(buildingId, "CRITICAL");
        long highRiskRooms = riskRepository.countByRoomFloorBuildingIdAndRiskLevel(buildingId, "HIGH_RISK");
        long warningRooms = riskRepository.countByRoomFloorBuildingIdAndRiskLevel(buildingId, "WARNING");
        long normalRooms = riskRepository.countByRoomFloorBuildingIdAndRiskLevel(buildingId, "NORMAL");

        long activeAnomalies = anomalyRepository.countByRoomFloorBuildingIdAndAnomalyDetectedTrue(buildingId);

        String overallLevel;
        if (criticalRooms > 0 || avgRisk >= 80.0) {
            overallLevel = "CRITICAL";
        } else if (highRiskRooms > 0 || avgRisk >= 60.0) {
            overallLevel = "HIGH_RISK";
        } else if (warningRooms > 0 || avgRisk >= 30.0) {
            overallLevel = "WARNING";
        } else {
            overallLevel = "NORMAL";
        }

        Map<String, Long> breakdown = Map.of(
                "CRITICAL", criticalRooms,
                "HIGH_RISK", highRiskRooms,
                "WARNING", warningRooms,
                "NORMAL", normalRooms
        );

        return new BuildingAiSummaryResponse(
                buildingId,
                building.getName(),
                totalRooms,
                avgRisk,
                overallLevel,
                activeAnomalies,
                criticalRooms,
                highRiskRooms,
                warningRooms,
                normalRooms,
                breakdown,
                Instant.now()
        );
    }

    @Override
    public Page<AnomalyDetection> getAnomalies(String buildingId, String floorId, String roomId, String severity, Pageable pageable) {
        if (roomId != null && !roomId.isBlank()) {
            return anomalyRepository.findByRoomIdAndOptionalSeverity(roomId, (severity != null && !severity.isBlank()) ? severity : null, pageable);
        }
        if (severity != null && !severity.isBlank() && buildingId != null) {
            return anomalyRepository.findByBuildingAndSeverity(buildingId, severity, pageable);
        }
        if (buildingId != null && !buildingId.isBlank()) {
            return anomalyRepository.findByRoomFloorBuildingIdOrderByDetectedAtDesc(buildingId, pageable);
        }
        return anomalyRepository.findAll(pageable);
    }

    @Override
    public Page<AiPrediction> getPredictions(String buildingId, String roomId, Pageable pageable) {
        if (roomId != null && !roomId.isBlank()) {
            return predictionRepository.findPagedByRoomId(roomId, pageable);
        }
        if (buildingId != null && !buildingId.isBlank()) {
            return predictionRepository.findByRoomFloorBuildingIdOrderByPredictionTimestampDesc(buildingId, pageable);
        }
        return predictionRepository.findAll(pageable);
    }

    @Override
    public Page<RiskAssessment> getRiskAssessments(String buildingId, String riskLevel, Pageable pageable) {
        if (riskLevel != null && !riskLevel.isBlank() && buildingId != null) {
            return riskRepository.findByBuildingAndLevel(buildingId, riskLevel, pageable);
        }
        if (buildingId != null && !buildingId.isBlank()) {
            return riskRepository.findByRoomFloorBuildingIdOrderByAssessedAtDesc(buildingId, pageable);
        }
        return riskRepository.findAll(pageable);
    }

    @Override
    public List<AiModelMetadataDto> getModelMetadata() {
        List<AiModelMetadataDto> remote = aiServiceClient.getModels();
        if (remote != null && !remote.isEmpty()) {
            return remote;
        }
        // Fallback to database metadata
        return modelMetadataRepository.findAll().stream()
                .map(m -> new AiModelMetadataDto(
                        m.getModelName(),
                        m.getModelVersion(),
                        m.getModelType(),
                        m.getTrainingDatasetSize(),
                        m.getTrainingTimestamp() != null ? m.getTrainingTimestamp().toString() : Instant.now().toString(),
                        Map.of(),
                        m.getModelStatus()
                ))
                .toList();
    }

    @Override
    public AiModelStatusResponse getModelStatus() {
        return aiServiceClient.getModelStatus();
    }

    @Override
    public AiModelTrainResponse trainModels(AiModelTrainRequest request) {
        return aiServiceClient.trainModels(request);
    }

    // --- Private Resilient Heuristic Fallbacks ---

    private AnomalyInferenceResponse calculateFallbackAnomaly(String roomId, double temp, double smoke, double aqi, int occ, int cap) {
        double score = 0.1;
        String sev = "NORMAL";
        boolean detected = false;
        String expl = "Nominal operating conditions.";

        if (smoke > 15.0) {
            score = 0.95;
            sev = "CRITICAL";
            detected = true;
            expl = String.format("Dangerous smoke concentration (%.1f ppm) detected via fallback safety guard.", smoke);
        } else if (temp > 45.0) {
            score = 0.88;
            sev = "CRITICAL";
            detected = true;
            expl = String.format("Critical heat surge (%.1f C) detected via fallback guard.", temp);
        } else if (smoke > 5.0 || temp > 35.0 || aqi > 200.0) {
            score = 0.70;
            sev = "HIGH";
            detected = true;
            expl = "Elevated environmental metrics exceeding standard safety bounds.";
        }

        return new AnomalyInferenceResponse(
                roomId,
                Instant.now().toString(),
                detected,
                score,
                sev,
                Map.of("temperature", temp, "smoke", smoke, "air_quality", aqi, "occupancy", (double) occ),
                expl,
                "v1.0.0-fallback"
        );
    }

    private OccupancyPredictionResponse calculateFallbackOccupancy(String roomId, int occ, int cap) {
        int pred = Math.min(cap, Math.max(0, occ));
        double util = Math.round(((double) pred / cap) * 1000.0) / 10.0;
        return new OccupancyPredictionResponse(
                roomId,
                occ,
                pred,
                15,
                util,
                Instant.now().toString(),
                "v1.0.0-fallback",
                "SUCCESS",
                0.80
        );
    }

    private RiskAssessmentResponse calculateFallbackRisk(String roomId, double temp, double smoke, double aqi, int occ, int cap, AnomalyInferenceResponse anom) {
        double tScore = Math.min(100.0, Math.max(0.0, (temp - 20.0) * 4.0));
        double sScore = Math.min(100.0, smoke * 5.0);
        double aScore = Math.min(100.0, (aqi / 300.0) * 100.0);
        double oScore = Math.min(100.0, ((double) occ / cap) * 100.0);

        double baseRisk = (0.35 * tScore) + (0.35 * sScore) + (0.20 * aScore) + (0.10 * oScore);
        boolean criticalOverride = false;
        String expl = "Calculated via multi-component risk formula.";

        if (smoke >= 15.0 || sScore >= 70.0) {
            baseRisk = Math.max(baseRisk, 90.0);
            criticalOverride = true;
            expl = "CRITICAL FIRE OVERRIDE: Dangerous smoke reading triggered maximum risk escalation.";
        }

        String level = (baseRisk >= 80.0) ? "CRITICAL" : (baseRisk >= 60.0) ? "HIGH_RISK" : (baseRisk >= 30.0) ? "WARNING" : "NORMAL";

        return new RiskAssessmentResponse(
                roomId,
                Math.round(baseRisk * 10.0) / 10.0,
                level,
                tScore,
                sScore,
                aScore,
                oScore,
                anom.anomalyScore() * 100.0,
                Map.of("temperature_score", tScore, "smoke_score", sScore, "air_quality_score", aScore, "occupancy_score", oScore),
                expl,
                Instant.now().toString(),
                criticalOverride
        );
    }
}
