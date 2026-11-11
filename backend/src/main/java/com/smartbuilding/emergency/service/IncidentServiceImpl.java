package com.smartbuilding.emergency.service;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.emergency.dto.*;
import com.smartbuilding.emergency.model.*;
import com.smartbuilding.emergency.repository.EmergencyAlertRepository;
import com.smartbuilding.emergency.repository.IncidentActivityRepository;
import com.smartbuilding.emergency.repository.IncidentRepository;
import com.smartbuilding.user.model.User;
import com.smartbuilding.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class IncidentServiceImpl implements IncidentService {

    private static final Logger logger = LoggerFactory.getLogger(IncidentServiceImpl.class);

    private final IncidentRepository incidentRepository;
    private final IncidentActivityRepository activityRepository;
    private final EmergencyAlertRepository alertRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final EmergencyRealtimeBroadcaster broadcaster;

    // Cooldown map to prevent duplicate alerts for the same room & category (cooldown: 3 minutes)
    private final Map<String, Instant> cooldownTracker = new ConcurrentHashMap<>();

    public IncidentServiceImpl(IncidentRepository incidentRepository,
                               IncidentActivityRepository activityRepository,
                               EmergencyAlertRepository alertRepository,
                               BuildingRepository buildingRepository,
                               FloorRepository floorRepository,
                               RoomRepository roomRepository,
                               UserRepository userRepository,
                               EmergencyRealtimeBroadcaster broadcaster) {
        this.incidentRepository = incidentRepository;
        this.activityRepository = activityRepository;
        this.alertRepository = alertRepository;
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.broadcaster = broadcaster;
    }

    @Override
    public IncidentResponse createIncident(CreateIncidentRequest request, String userId, String userName) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + request.getBuildingId()));

        Floor floor = null;
        if (request.getFloorId() != null) {
            floor = floorRepository.findById(request.getFloorId()).orElse(null);
        }

        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId()).orElse(null);
            if (room != null && floor == null) {
                floor = room.getFloor();
            }
        }

        String incidentId = UUID.randomUUID().toString();
        String refNumber = generateReferenceNumber();

        Incident incident = new Incident(
                incidentId,
                refNumber,
                building,
                floor,
                room,
                request.getCategory(),
                request.getSeverity(),
                IncidentStatus.DETECTED,
                request.getDetectionSource() != null ? request.getDetectionSource() : IncidentSource.MANUAL,
                request.getTitle(),
                request.getDescription()
        );

        Incident saved = incidentRepository.save(incident);

        // Record initial creation activity
        IncidentActivity activity = new IncidentActivity(
                UUID.randomUUID().toString(),
                saved,
                "INCIDENT_REPORTED",
                "Incident logged with severity " + saved.getSeverity() + " via " + saved.getDetectionSource(),
                userId != null ? userId : "SYSTEM",
                userName != null ? userName : "Automated Subsystem"
        );
        activityRepository.save(activity);
        saved.getActivities().add(activity);

        // Create initial Emergency Alert if severity is HIGH or CRITICAL
        if (saved.getSeverity() == IncidentSeverity.HIGH || saved.getSeverity() == IncidentSeverity.CRITICAL) {
            EmergencyAlert alert = new EmergencyAlert(
                    UUID.randomUUID().toString(),
                    saved,
                    building,
                    saved.getCategory().name(),
                    saved.getSeverity(),
                    "EMERGENCY: " + saved.getTitle(),
                    saved.getDescription() != null ? saved.getDescription() : "Emergency incident active in " + (room != null ? room.getRoomName() : building.getName())
            );
            alertRepository.save(alert);

            broadcaster.broadcastAlert(building.getId(), Map.of(
                    "alertId", alert.getId(),
                    "incidentId", saved.getId(),
                    "severity", saved.getSeverity().name(),
                    "title", saved.getTitle(),
                    "timestamp", Instant.now().toString()
            ));
        }

        IncidentResponse response = IncidentResponse.fromEntity(saved);
        broadcaster.broadcastIncidentUpdate(building.getId(), response);
        logger.info("Created incident {} ({}) in building {}", saved.getReferenceNumber(), saved.getCategory(), building.getName());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentResponse getIncident(String id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Incident not found with ID: " + id));
        return IncidentResponse.fromEntity(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getFilteredIncidents(String buildingId, IncidentStatus status,
                                                      IncidentSeverity severity, IncidentCategory category,
                                                      int page, int size) {
        int clampedPage = Math.max(0, page);
        int clampedSize = Math.min(100, Math.max(1, size));
        Pageable pageable = PageRequest.of(clampedPage, clampedSize);
        return incidentRepository.findFilteredIncidents(buildingId, status, severity, category, pageable)
                .map(IncidentResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> getActiveBuildingIncidents(String buildingId) {
        List<IncidentStatus> activeStatuses = List.of(
                IncidentStatus.DETECTED, IncidentStatus.ACTIVE,
                IncidentStatus.ACKNOWLEDGED, IncidentStatus.RESPONDING
        );
        return incidentRepository.findByBuildingIdAndStatusInOrderByDetectedAtDesc(buildingId, activeStatuses)
                .stream()
                .map(IncidentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public IncidentResponse updateIncidentStatus(String id, UpdateIncidentStatusRequest request, String userId, String userName) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Incident not found with ID: " + id));

        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(request.getStatus());

        if (request.getStatus() == IncidentStatus.ACKNOWLEDGED && incident.getAcknowledgedAt() == null) {
            incident.setAcknowledgedAt(Instant.now());
        } else if (request.getStatus() == IncidentStatus.RESOLVED && incident.getResolvedAt() == null) {
            incident.setResolvedAt(Instant.now());
        } else if (request.getStatus() == IncidentStatus.CLOSED && incident.getClosedAt() == null) {
            incident.setClosedAt(Instant.now());
        }

        Incident saved = incidentRepository.save(incident);

        String details = "Status changed from " + oldStatus + " to " + request.getStatus();
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            details += ". Note: " + request.getNotes().trim();
        }

        IncidentActivity activity = new IncidentActivity(
                UUID.randomUUID().toString(),
                saved,
                "STATUS_UPDATE",
                details,
                userId != null ? userId : "SYSTEM",
                userName != null ? userName : "Officer"
        );
        activityRepository.save(activity);
        saved.getActivities().add(0, activity);

        IncidentResponse response = IncidentResponse.fromEntity(saved);
        broadcaster.broadcastIncidentUpdate(saved.getBuilding().getId(), response);
        logger.info("Updated incident {} status to {}", saved.getReferenceNumber(), saved.getStatus());
        return response;
    }

    @Override
    public IncidentResponse assignIncident(String id, AssignIncidentRequest request, String assignedByUserId, String assignedByUserName) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("Incident not found with ID: " + id));

        User officer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> ServiceException.notFound("User not found with ID: " + request.getUserId()));

        incident.setAssignedOfficer(officer);
        if (incident.getStatus() == IncidentStatus.DETECTED) {
            incident.setStatus(IncidentStatus.ACKNOWLEDGED);
            incident.setAcknowledgedAt(Instant.now());
        }

        Incident saved = incidentRepository.save(incident);

        String details = "Assigned to officer " + officer.getFullName();
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            details += ". Note: " + request.getNotes().trim();
        }

        IncidentActivity activity = new IncidentActivity(
                UUID.randomUUID().toString(),
                saved,
                "OFFICER_ASSIGNED",
                details,
                assignedByUserId != null ? assignedByUserId : "SYSTEM",
                assignedByUserName != null ? assignedByUserName : "Manager"
        );
        activityRepository.save(activity);
        saved.getActivities().add(0, activity);

        IncidentResponse response = IncidentResponse.fromEntity(saved);
        broadcaster.broadcastIncidentUpdate(saved.getBuilding().getId(), response);
        return response;
    }

    @Override
    public IncidentResponse acknowledgeIncident(String id, String userId, String userName) {
        UpdateIncidentStatusRequest req = new UpdateIncidentStatusRequest();
        req.setStatus(IncidentStatus.ACKNOWLEDGED);
        req.setNotes("Incident acknowledged by safety officer");
        return updateIncidentStatus(id, req, userId, userName);
    }

    @Override
    public IncidentResponse resolveIncident(String id, String resolutionNotes, String userId, String userName) {
        UpdateIncidentStatusRequest req = new UpdateIncidentStatusRequest();
        req.setStatus(IncidentStatus.RESOLVED);
        req.setNotes(resolutionNotes != null ? resolutionNotes : "Incident verified safe and resolved.");
        return updateIncidentStatus(id, req, userId, userName);
    }

    @Override
    public void processSensorOrAiDetection(String buildingId, String roomId, IncidentCategory category,
                                          IncidentSeverity severity, IncidentSource source,
                                          String title, String description) {
        if (buildingId == null) return;

        // Deduplication & cooldown check (180 seconds window)
        String dedupeKey = (roomId != null ? roomId : buildingId) + "_" + category.name();
        Instant lastTrigger = cooldownTracker.get(dedupeKey);
        if (lastTrigger != null && Instant.now().isBefore(lastTrigger.plus(180, ChronoUnit.SECONDS))) {
            logger.debug("Deduplication: suppressing repeated alert for key {}", dedupeKey);
            return;
        }

        // Check if there is already an active incident in this room for this category
        if (roomId != null) {
            List<Incident> existing = incidentRepository.findActiveIncidentForRoomAndCategory(
                    roomId, category, Instant.now().minus(10, ChronoUnit.MINUTES)
            );
            if (!existing.isEmpty()) {
                Incident current = existing.get(0);
                logger.debug("Active incident {} already exists for room {}", current.getReferenceNumber(), roomId);
                cooldownTracker.put(dedupeKey, Instant.now());
                return;
            }
        }

        cooldownTracker.put(dedupeKey, Instant.now());

        CreateIncidentRequest req = new CreateIncidentRequest();
        req.setBuildingId(buildingId);
        req.setRoomId(roomId);
        req.setCategory(category);
        req.setSeverity(severity);
        req.setDetectionSource(source);
        req.setTitle(title);
        req.setDescription(description);

        try {
            createIncident(req, "SYSTEM", source.name() + " Automated Detector");
        } catch (Exception e) {
            logger.error("Failed to auto-generate emergency incident: {}", e.getMessage(), e);
        }
    }

    private String generateReferenceNumber() {
        long timestamp = System.currentTimeMillis() % 1000000;
        int randomSuffix = 100 + new Random().nextInt(900);
        return "INC-" + timestamp + "-" + randomSuffix;
    }
}
