package com.smartbuilding.sensor.service;

import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.sensor.dto.*;
import com.smartbuilding.sensor.model.*;
import com.smartbuilding.sensor.repository.SensorReadingRepository;
import com.smartbuilding.sensor.repository.SensorRepository;
import com.smartbuilding.simulation.model.SessionStatus;
import com.smartbuilding.simulation.repository.SimulationSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SensorServiceImpl implements SensorService {

    private static final Logger logger = LoggerFactory.getLogger(SensorServiceImpl.class);

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository readingRepository;
    private final RoomRepository roomRepository;
    private final SimulationSessionRepository sessionRepository;
    private final SensorRealtimeBroadcaster broadcaster;

    public SensorServiceImpl(
            SensorRepository sensorRepository,
            SensorReadingRepository readingRepository,
            RoomRepository roomRepository,
            SimulationSessionRepository sessionRepository,
            SensorRealtimeBroadcaster broadcaster) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.roomRepository = roomRepository;
        this.sessionRepository = sessionRepository;
        this.broadcaster = broadcaster;
    }

    @Override
    public SensorResponse createSensor(String roomId, CreateSensorRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));

        if (sensorRepository.existsByRoomIdAndSensorType(roomId, request.getSensorType())) {
            throw ServiceException.conflict("Room already has a sensor of type " + request.getSensorType());
        }

        SensorUnit unit = request.getUnit() != null ? request.getUnit() : getDefaultUnit(request.getSensorType());
        String sensorCode = request.getSensorCode();
        if (sensorCode == null || sensorCode.trim().isEmpty()) {
            sensorCode = generateSensorCode(room, request.getSensorType());
        }

        if (sensorRepository.findBySensorCode(sensorCode).isPresent()) {
            sensorCode = sensorCode + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }

        Sensor sensor = new Sensor(room, sensorCode, request.getSensorName(), request.getSensorType(), unit);
        if (request.getSimulationIntervalSeconds() != null && request.getSimulationIntervalSeconds() > 0) {
            sensor.setSimulationIntervalSeconds(request.getSimulationIntervalSeconds());
        }

        double initialValue = getDefaultInitialValue(request.getSensorType());
        sensor.setLastReadingValue(initialValue);
        sensor.setLastReadingAt(Instant.now());

        Sensor saved = sensorRepository.save(sensor);

        // Store initial baseline reading
        SensorReading initialReading = new SensorReading(saved, initialValue, ReadingSource.SIMULATED, null);
        readingRepository.save(initialReading);

        logger.info("Created sensor {} [{}] for room {}", saved.getSensorCode(), saved.getSensorType(), room.getRoomName());
        return SensorResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorResponse> getSensorsByRoom(String roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw ServiceException.notFound("Room not found with ID: " + roomId);
        }
        return sensorRepository.findByRoomId(roomId).stream()
                .map(SensorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorResponse> getSensors(String buildingId, String floorId, String roomId, SensorType type, SensorStatus status) {
        List<Sensor> list;
        if (roomId != null && !roomId.trim().isEmpty()) {
            list = sensorRepository.findByRoomId(roomId);
        } else if (floorId != null && !floorId.trim().isEmpty()) {
            list = sensorRepository.findByFloorId(floorId);
        } else if (buildingId != null && !buildingId.trim().isEmpty()) {
            list = sensorRepository.findByBuildingId(buildingId);
        } else {
            list = sensorRepository.findAll();
        }

        return list.stream()
                .filter(s -> type == null || s.getSensorType() == type)
                .filter(s -> status == null || s.getStatus() == status)
                .map(SensorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SensorResponse getSensorById(String sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> ServiceException.notFound("Sensor not found with ID: " + sensorId));
        return SensorResponse.fromEntity(sensor);
    }

    @Override
    public SensorResponse updateSensor(String sensorId, UpdateSensorRequest request) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> ServiceException.notFound("Sensor not found with ID: " + sensorId));

        if (request.getSensorName() != null && !request.getSensorName().trim().isEmpty()) {
            sensor.setSensorName(request.getSensorName().trim());
        }
        if (request.getSimulationIntervalSeconds() != null && request.getSimulationIntervalSeconds() > 0) {
            sensor.setSimulationIntervalSeconds(request.getSimulationIntervalSeconds());
        }
        if (request.getStatus() != null) {
            sensor.setStatus(request.getStatus());
        }

        Sensor saved = sensorRepository.save(sensor);
        return SensorResponse.fromEntity(saved);
    }

    @Override
    public SensorResponse updateSensorStatus(String sensorId, SensorStatus status) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> ServiceException.notFound("Sensor not found with ID: " + sensorId));
        sensor.setStatus(status);
        Sensor saved = sensorRepository.save(sensor);
        return SensorResponse.fromEntity(saved);
    }

    @Override
    public void deleteSensor(String sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw ServiceException.notFound("Sensor not found with ID: " + sensorId);
        }
        sensorRepository.deleteById(sensorId);
        logger.info("Deleted sensor with ID: {}", sensorId);
    }

    @Override
    public List<SensorResponse> provisionDefaultSensors(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ServiceException.notFound("Room not found with ID: " + roomId));

        List<SensorResponse> responses = new ArrayList<>();

        for (SensorType type : SensorType.values()) {
            if (!sensorRepository.existsByRoomIdAndSensorType(roomId, type)) {
                CreateSensorRequest req = new CreateSensorRequest();
                req.setSensorType(type);
                req.setSensorName(buildSensorName(room, type));
                req.setSensorCode(generateSensorCode(room, type));
                req.setUnit(getDefaultUnit(type));
                req.setSimulationIntervalSeconds(5);

                responses.add(createSensor(roomId, req));
            } else {
                sensorRepository.findByRoomIdAndSensorType(roomId, type)
                        .ifPresent(s -> responses.add(SensorResponse.fromEntity(s)));
            }
        }

        logger.info("Default sensor suite verified/provisioned for room {} (Total: {})", room.getRoomName(), responses.size());
        return responses;
    }

    @Override
    public List<SensorResponse> provisionBuildingSensors(String buildingId) {
        List<Room> rooms = roomRepository.findByFloorBuildingId(buildingId);
        List<SensorResponse> totalProvisioned = new ArrayList<>();
        for (Room room : rooms) {
            totalProvisioned.addAll(provisionDefaultSensors(room.getId()));
        }
        logger.info("Provisioned default sensor suites across building {} (Total sensors: {})", buildingId, totalProvisioned.size());
        return totalProvisioned;
    }

    @Override
    public SensorReadingResponse recordManualReading(String sensorId, double readingValue) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> ServiceException.notFound("Sensor not found with ID: " + sensorId));

        validateReadingValue(sensor, readingValue);

        sensor.setLastReadingValue(readingValue);
        sensor.setLastReadingAt(Instant.now());
        sensorRepository.save(sensor);

        SensorReading reading = new SensorReading(sensor, readingValue, ReadingSource.MANUAL, null);
        SensorReading saved = readingRepository.save(reading);

        // Synchronize room occupancy when Occupancy Sensor is injected
        if (sensor.getSensorType() == SensorType.OCCUPANCY && sensor.getRoom() != null) {
            Room room = sensor.getRoom();
            int clampedOccupancy = Math.min((int) Math.round(readingValue), room.getMaximumCapacity());
            clampedOccupancy = Math.max(0, clampedOccupancy);
            room.setCurrentOccupancy(clampedOccupancy);
            roomRepository.save(room);

            String bldId = room.getFloor() != null && room.getFloor().getBuilding() != null
                    ? room.getFloor().getBuilding().getId() : null;
            broadcaster.broadcastRoomOccupancy(bldId, room.getId(), clampedOccupancy, room.getMaximumCapacity());
        }

        SensorReadingResponse response = SensorReadingResponse.fromEntity(saved);

        String bldId = (sensor.getRoom() != null && sensor.getRoom().getFloor() != null && sensor.getRoom().getFloor().getBuilding() != null)
                ? sensor.getRoom().getFloor().getBuilding().getId() : null;
        broadcaster.broadcastSensorReading(bldId, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SensorStatsResponse getSensorStats() {
        long total = sensorRepository.count();
        long active = sensorRepository.countByStatus(SensorStatus.ACTIVE);
        long inactive = sensorRepository.countByStatus(SensorStatus.INACTIVE);
        long error = sensorRepository.countByStatus(SensorStatus.ERROR);

        boolean isRunning = !sessionRepository.findByStatus(SessionStatus.RUNNING).isEmpty();
        String activeSessionId = sessionRepository.findByStatus(SessionStatus.RUNNING).stream()
                .findFirst()
                .map(s -> s.getId())
                .orElse(null);

        Map<String, Long> distribution = new HashMap<>();
        for (SensorType type : SensorType.values()) {
            distribution.put(type.name(), sensorRepository.countBySensorType(type));
        }

        return new SensorStatsResponse(total, active, inactive, error, isRunning, activeSessionId, distribution);
    }

    // Helper methods

    private SensorUnit getDefaultUnit(SensorType type) {
        return switch (type) {
            case TEMPERATURE -> SensorUnit.CELSIUS;
            case HUMIDITY -> SensorUnit.PERCENT;
            case SMOKE -> SensorUnit.PPM;
            case AIR_QUALITY -> SensorUnit.AQI;
            case OCCUPANCY -> SensorUnit.PEOPLE;
        };
    }

    private double getDefaultInitialValue(SensorType type) {
        return switch (type) {
            case TEMPERATURE -> 22.5;
            case HUMIDITY -> 45.0;
            case SMOKE -> 0.01;
            case AIR_QUALITY -> 40.0;
            case OCCUPANCY -> 0.0;
        };
    }

    private String buildSensorName(Room room, SensorType type) {
        String typeLabel = switch (type) {
            case TEMPERATURE -> "Temperature Sensor";
            case HUMIDITY -> "Humidity Sensor";
            case SMOKE -> "Smoke Detector (Simulated)";
            case AIR_QUALITY -> "Air Quality Index Sensor";
            case OCCUPANCY -> "Occupancy Counter";
        };
        return room.getRoomName() + " " + typeLabel;
    }

    private String generateSensorCode(Room room, SensorType type) {
        String codePrefix = switch (type) {
            case TEMPERATURE -> "TMP";
            case HUMIDITY -> "HUM";
            case SMOKE -> "SMK";
            case AIR_QUALITY -> "AQI";
            case OCCUPANCY -> "OCC";
        };
        String roomToken = room.getRoomCode().replaceAll("[^A-Za-z0-9]", "-").toUpperCase();
        return "SNS-" + roomToken + "-" + codePrefix;
    }

    private void validateReadingValue(Sensor sensor, double value) {
        if (sensor.getSensorType() == SensorType.OCCUPANCY) {
            if (value < 0) {
                throw ServiceException.badRequest("Occupancy reading cannot be negative");
            }
            if (sensor.getRoom() != null && value > sensor.getRoom().getMaximumCapacity()) {
                throw ServiceException.badRequest("Occupancy reading (" + value + ") exceeds room maximum capacity ("
                        + sensor.getRoom().getMaximumCapacity() + ")");
            }
        }
        if (sensor.getSensorType() == SensorType.SMOKE && value < 0) {
            throw ServiceException.badRequest("Smoke concentration cannot be negative");
        }
        if (sensor.getSensorType() == SensorType.AIR_QUALITY && (value < 0 || value > 500)) {
            throw ServiceException.badRequest("AQI reading must be between 0 and 500");
        }
        if (sensor.getSensorType() == SensorType.HUMIDITY && (value < 0 || value > 100)) {
            throw ServiceException.badRequest("Relative humidity percentage must be between 0% and 100%");
        }
    }
}
