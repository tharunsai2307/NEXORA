package com.smartbuilding.digitaltwin.service;

import com.smartbuilding.ai.model.AiPrediction;
import com.smartbuilding.ai.model.AnomalyDetection;
import com.smartbuilding.ai.model.RiskAssessment;
import com.smartbuilding.ai.repository.AiPredictionRepository;
import com.smartbuilding.ai.repository.AnomalyDetectionRepository;
import com.smartbuilding.ai.repository.RiskAssessmentRepository;
import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.digitaltwin.dto.DigitalTwinBuildingResponse;
import com.smartbuilding.digitaltwin.dto.DigitalTwinFloorResponse;
import com.smartbuilding.digitaltwin.dto.DigitalTwinRoomResponse;
import com.smartbuilding.digitaltwin.dto.RoomLayoutUpdateRequest;
import com.smartbuilding.sensor.model.Sensor;
import com.smartbuilding.sensor.repository.SensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DigitalTwinServiceImpl implements DigitalTwinService {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinServiceImpl.class);

    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final SensorRepository sensorRepository;
    private final RiskAssessmentRepository riskRepository;
    private final AnomalyDetectionRepository anomalyRepository;
    private final AiPredictionRepository predictionRepository;

    public DigitalTwinServiceImpl(
            BuildingRepository buildingRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            SensorRepository sensorRepository,
            RiskAssessmentRepository riskRepository,
            AnomalyDetectionRepository anomalyRepository,
            AiPredictionRepository predictionRepository) {
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.sensorRepository = sensorRepository;
        this.riskRepository = riskRepository;
        this.anomalyRepository = anomalyRepository;
        this.predictionRepository = predictionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DigitalTwinBuildingResponse getDigitalTwinBuilding(String buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        List<Floor> floors = floorRepository.findByBuildingIdOrderByFloorNumberAsc(buildingId);
        List<DigitalTwinFloorResponse> floorResponses = new ArrayList<>();

        int totalBuildingRooms = 0;
        int totalBuildingCapacity = 0;
        int totalBuildingOccupancy = 0;
        double totalRiskScoreSum = 0.0;
        int totalEvaluatedRooms = 0;

        long criticalCount = 0;
        long highRiskCount = 0;
        long warningCount = 0;
        long normalCount = 0;

        for (Floor floor : floors) {
            List<Room> rooms = roomRepository.findByFloorIdOrderByRoomCodeAsc(floor.getId());
            List<DigitalTwinRoomResponse> roomResponses = new ArrayList<>();

            int floorCap = 0;
            int floorOcc = 0;

            for (Room room : rooms) {
                totalBuildingRooms++;
                floorCap += room.getMaximumCapacity();
                floorOcc += room.getCurrentOccupancy();

                // Fetch latest AI telemetry
                Optional<RiskAssessment> latestRisk = riskRepository.findFirstByRoomIdOrderByAssessedAtDesc(room.getId());
                Optional<AnomalyDetection> latestAnom = anomalyRepository.findFirstByRoomIdOrderByDetectedAtDesc(room.getId());
                Optional<AiPrediction> latestPred = predictionRepository.findFirstByRoomIdOrderByPredictionTimestampDesc(room.getId());

                // Fetch latest sensor readings
                List<Sensor> sensors = sensorRepository.findByRoomId(room.getId());
                Map<String, Double> sensorReadings = new HashMap<>();
                for (Sensor s : sensors) {
                    if (s.getLastReadingValue() != null) {
                        sensorReadings.put(s.getSensorType().name(), s.getLastReadingValue());
                    }
                }

                Double rScore = latestRisk.map(RiskAssessment::getRiskScore).orElse(10.0);
                String rLevel = latestRisk.map(RiskAssessment::getRiskLevel).orElse("NORMAL");
                String rExpl = latestRisk.map(RiskAssessment::getExplanation).orElse("Nominal climate and safety baseline.");

                totalRiskScoreSum += rScore;
                totalEvaluatedRooms++;

                if ("CRITICAL".equalsIgnoreCase(rLevel)) criticalCount++;
                else if ("HIGH_RISK".equalsIgnoreCase(rLevel)) highRiskCount++;
                else if ("WARNING".equalsIgnoreCase(rLevel)) warningCount++;
                else normalCount++;

                Double predVal = latestPred.map(AiPrediction::getPredictedValue).orElse((double) room.getCurrentOccupancy());
                Boolean anomDet = latestAnom.map(AnomalyDetection::getAnomalyDetected).orElse(false);
                String anomSev = latestAnom.map(AnomalyDetection::getSeverity).orElse("NORMAL");
                Double anomScr = latestAnom.map(AnomalyDetection::getAnomalyScore).orElse(0.1);
                String anomExpl = latestAnom.map(AnomalyDetection::getExplanation).orElse("Nominal conditions.");

                int cap = Math.max(1, room.getMaximumCapacity());
                double utilPct = Math.round(((double) room.getCurrentOccupancy() / cap) * 1000.0) / 10.0;

                roomResponses.add(new DigitalTwinRoomResponse(
                        room.getId(),
                        floor.getId(),
                        floor.getFloorNumber(),
                        room.getRoomName(),
                        room.getRoomCode(),
                        room.getRoomType().name(),
                        room.getDescription(),
                        room.getMaximumCapacity(),
                        room.getCurrentOccupancy(),
                        utilPct,
                        room.getPosX(),
                        room.getPosY(),
                        room.getPosZ(),
                        room.getWidth(),
                        room.getDepth(),
                        room.getHeight(),
                        room.isActive(),
                        room.getSafetyStatus().name(),
                        rScore,
                        rLevel,
                        rExpl,
                        predVal,
                        anomDet,
                        anomSev,
                        anomScr,
                        anomExpl,
                        sensorReadings
                ));
            }

            totalBuildingCapacity += floorCap;
            totalBuildingOccupancy += floorOcc;

            floorResponses.add(new DigitalTwinFloorResponse(
                    floor.getId(),
                    buildingId,
                    floor.getFloorName(),
                    floor.getFloorNumber(),
                    floor.getDescription(),
                    floor.getLayoutWidth(),
                    floor.getLayoutDepth(),
                    floor.getStatus().name(),
                    rooms.size(),
                    floorCap,
                    floorOcc,
                    roomResponses
            ));
        }

        double avgRisk = totalEvaluatedRooms > 0
                ? Math.round((totalRiskScoreSum / totalEvaluatedRooms) * 10.0) / 10.0
                : 12.0;

        String overallLevel;
        if (criticalCount > 0 || avgRisk >= 80.0) overallLevel = "CRITICAL";
        else if (highRiskCount > 0 || avgRisk >= 60.0) overallLevel = "HIGH_RISK";
        else if (warningCount > 0 || avgRisk >= 30.0) overallLevel = "WARNING";
        else overallLevel = "NORMAL";

        return new DigitalTwinBuildingResponse(
                building.getId(),
                building.getName(),
                building.getBuildingCode(),
                building.getBuildingType().name(),
                building.getAddress(),
                floors.size(),
                building.getStatus().name(),
                totalBuildingRooms,
                totalBuildingCapacity,
                totalBuildingOccupancy,
                avgRisk,
                overallLevel,
                criticalCount,
                highRiskCount,
                warningCount,
                normalCount,
                floorResponses
        );
    }

    @Override
    @Transactional
    public DigitalTwinRoomResponse updateRoomLayout(String roomId, RoomLayoutUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));

        room.setPosX(request.posX());
        room.setPosY(request.posY());
        room.setPosZ(request.posZ());
        room.setWidth(request.width());
        room.setDepth(request.depth());
        room.setHeight(request.height());

        Room updated = roomRepository.save(room);
        logger.info("Updated 3D spatial layout for room {}: pos=({},{},{}) dims=({},{},{})",
                roomId, updated.getPosX(), updated.getPosY(), updated.getPosZ(),
                updated.getWidth(), updated.getDepth(), updated.getHeight());

        int cap = Math.max(1, updated.getMaximumCapacity());
        double utilPct = Math.round(((double) updated.getCurrentOccupancy() / cap) * 1000.0) / 10.0;

        return new DigitalTwinRoomResponse(
                updated.getId(),
                updated.getFloor().getId(),
                updated.getFloor().getFloorNumber(),
                updated.getRoomName(),
                updated.getRoomCode(),
                updated.getRoomType().name(),
                updated.getDescription(),
                updated.getMaximumCapacity(),
                updated.getCurrentOccupancy(),
                utilPct,
                updated.getPosX(),
                updated.getPosY(),
                updated.getPosZ(),
                updated.getWidth(),
                updated.getDepth(),
                updated.getHeight(),
                updated.isActive(),
                updated.getSafetyStatus().name(),
                12.0,
                "NORMAL",
                "Layout updated.",
                (double) updated.getCurrentOccupancy(),
                false,
                "NORMAL",
                0.1,
                "Nominal.",
                Map.of()
        );
    }
}
