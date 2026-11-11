package com.smartbuilding.emergency.service;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.emergency.dto.*;
import com.smartbuilding.emergency.model.*;
import com.smartbuilding.emergency.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvacuationSessionServiceImpl implements EvacuationSessionService {

    private static final Logger logger = LoggerFactory.getLogger(EvacuationSessionServiceImpl.class);

    private final EvacuationSessionRepository sessionRepository;
    private final EvacuationRoomStatusRepository roomStatusRepository;
    private final EvacuationNodeRepository nodeRepository;
    private final EvacuationConnectionRepository connectionRepository;
    private final EvacuationRouteRepository routeRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    private final EvacuationRoutingService routingService;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final EmergencyRealtimeBroadcaster broadcaster;

    public EvacuationSessionServiceImpl(EvacuationSessionRepository sessionRepository,
                                       EvacuationRoomStatusRepository roomStatusRepository,
                                       EvacuationNodeRepository nodeRepository,
                                       EvacuationConnectionRepository connectionRepository,
                                       EvacuationRouteRepository routeRepository,
                                       IncidentRepository incidentRepository,
                                       IncidentService incidentService,
                                       EvacuationRoutingService routingService,
                                       BuildingRepository buildingRepository,
                                       RoomRepository roomRepository,
                                       EmergencyRealtimeBroadcaster broadcaster) {
        this.sessionRepository = sessionRepository;
        this.roomStatusRepository = roomStatusRepository;
        this.nodeRepository = nodeRepository;
        this.connectionRepository = connectionRepository;
        this.routeRepository = routeRepository;
        this.incidentRepository = incidentRepository;
        this.incidentService = incidentService;
        this.routingService = routingService;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.broadcaster = broadcaster;
    }

    @Override
    public EvacuationSessionResponse startEvacuation(String buildingId, StartEvacuationRequest request,
                                                    String userId, String userName) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        // Optional linked incident
        Incident incident = null;
        if (request != null && request.getIncidentId() != null) {
            incident = incidentRepository.findById(request.getIncidentId()).orElse(null);
        }

        String sessionId = UUID.randomUUID().toString();
        String sessionName = (request != null && request.getSessionName() != null && !request.getSessionName().trim().isEmpty())
                ? request.getSessionName().trim()
                : "Emergency Evacuation - " + building.getName();

        EvacuationSession session = new EvacuationSession(
                sessionId,
                incident,
                building,
                sessionName,
                userName != null ? userName : (userId != null ? userId : "Command Center")
        );
        session.setSessionStatus(EvacuationSessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setNotes(request != null ? request.getNotes() : null);

        List<Room> rooms = roomRepository.findByFloorBuildingId(buildingId);
        int totalOccupants = 0;
        int clearedCount = 0;
        int evacuatingCount = 0;

        List<EvacuationRoomStatus> roomStatuses = new ArrayList<>();
        for (Room r : rooms) {
            int occ = Math.max(0, r.getCurrentOccupancy());
            totalOccupants += occ;

            // Simulated attribute: 1 assistance needed if occupied, 2 if heavily occupied (>10)
            int assist = occ > 10 ? 2 : (occ > 0 ? 1 : 0);

            EvacuationRoomStatus status = new EvacuationRoomStatus(
                    UUID.randomUUID().toString(),
                    session,
                    r,
                    occ,
                    assist
            );
            if (occ == 0) {
                clearedCount++;
            } else {
                evacuatingCount++;
            }
            roomStatuses.add(status);
        }

        session.setTotalRooms(rooms.size());
        session.setClearedRooms(clearedCount);
        session.setEvacuatingRooms(evacuatingCount);
        session.setBlockedRooms(0);
        session.setTotalInitialOccupants(totalOccupants);
        session.setRemainingOccupants(totalOccupants);
        session.setRoomStatuses(roomStatuses);

        EvacuationSession saved = sessionRepository.save(session);
        roomStatusRepository.saveAll(roomStatuses);

        // Calculate and persist routes for this session
        routingService.calculateRoutesForBuilding(buildingId, saved.getId());

        EvacuationSessionResponse response = EvacuationSessionResponse.fromEntity(saved);
        broadcaster.broadcastEvacuationUpdate(buildingId, response);
        logger.info("Started evacuation session {} for building {}", saved.getId(), building.getName());
        return response;
    }

    @Override
    public EvacuationSessionResponse pauseEvacuation(String sessionId, String userId, String userName) {
        EvacuationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Evacuation session not found with ID: " + sessionId));

        session.setSessionStatus(EvacuationSessionStatus.PAUSED);
        EvacuationSession saved = sessionRepository.save(session);

        EvacuationSessionResponse response = EvacuationSessionResponse.fromEntity(saved);
        broadcaster.broadcastEvacuationUpdate(saved.getBuilding().getId(), response);
        logger.info("Paused evacuation session {}", sessionId);
        return response;
    }

    @Override
    public EvacuationSessionResponse resumeEvacuation(String sessionId, String userId, String userName) {
        EvacuationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Evacuation session not found with ID: " + sessionId));

        session.setSessionStatus(EvacuationSessionStatus.ACTIVE);
        EvacuationSession saved = sessionRepository.save(session);

        EvacuationSessionResponse response = EvacuationSessionResponse.fromEntity(saved);
        broadcaster.broadcastEvacuationUpdate(saved.getBuilding().getId(), response);
        logger.info("Resumed evacuation session {}", sessionId);
        return response;
    }

    @Override
    public EvacuationSessionResponse cancelEvacuation(String sessionId, String userId, String userName) {
        EvacuationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Evacuation session not found with ID: " + sessionId));

        session.setSessionStatus(EvacuationSessionStatus.CANCELLED);
        session.setEndedAt(Instant.now());
        EvacuationSession saved = sessionRepository.save(session);

        EvacuationSessionResponse response = EvacuationSessionResponse.fromEntity(saved);
        broadcaster.broadcastEvacuationUpdate(saved.getBuilding().getId(), response);
        logger.info("Cancelled evacuation session {}", sessionId);
        return response;
    }

    @Override
    public EvacuationSessionResponse stepEvacuationProgress(String sessionId) {
        EvacuationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Evacuation session not found with ID: " + sessionId));

        if (session.getSessionStatus() != EvacuationSessionStatus.ACTIVE) {
            return EvacuationSessionResponse.fromEntity(session);
        }

        List<EvacuationRoomStatus> statuses = roomStatusRepository.findBySessionId(sessionId);
        int remainingTotal = 0;
        int clearedCount = 0;
        int evacuatingCount = 0;

        for (EvacuationRoomStatus rs : statuses) {
            if (rs.getStatus() == RoomEvacuationState.EVACUATING) {
                int currentRem = rs.getRemainingOccupants();
                if (currentRem > 0) {
                    // Evacuate 35% of occupants or at least 3 people per simulation tick
                    int stepOut = Math.max(2, (int) Math.ceil(currentRem * 0.35));
                    int newRem = Math.max(0, currentRem - stepOut);
                    rs.setRemainingOccupants(newRem);

                    // Update actual room occupancy
                    Room room = rs.getRoom();
                    room.setCurrentOccupancy(newRem);
                    roomRepository.save(room);

                    if (newRem == 0) {
                        rs.setStatus(RoomEvacuationState.CLEARED);
                        rs.setOccupantsNeedingAssistance(0);
                        rs.setClearedAt(Instant.now());
                        clearedCount++;
                    } else {
                        evacuatingCount++;
                        remainingTotal += newRem;
                    }
                } else {
                    rs.setStatus(RoomEvacuationState.CLEARED);
                    rs.setClearedAt(Instant.now());
                    clearedCount++;
                }
            } else if (rs.getStatus() == RoomEvacuationState.CLEARED) {
                clearedCount++;
            } else {
                remainingTotal += rs.getRemainingOccupants();
            }
        }

        roomStatusRepository.saveAll(statuses);

        session.setClearedRooms(clearedCount);
        session.setEvacuatingRooms(evacuatingCount);
        session.setRemainingOccupants(remainingTotal);

        if (evacuatingCount == 0 && remainingTotal == 0) {
            session.setSessionStatus(EvacuationSessionStatus.COMPLETED);
            session.setEndedAt(Instant.now());
            logger.info("Evacuation session {} completed successfully! All rooms cleared.", sessionId);
        }

        EvacuationSession saved = sessionRepository.save(session);
        saved.setRoomStatuses(statuses);

        EvacuationSessionResponse response = EvacuationSessionResponse.fromEntity(saved);
        broadcaster.broadcastEvacuationUpdate(saved.getBuilding().getId(), response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public EvacuationSessionResponse getActiveSession(String buildingId) {
        return sessionRepository.findFirstByBuildingIdAndSessionStatusInOrderByCreatedAtDesc(
                buildingId, List.of(EvacuationSessionStatus.ACTIVE, EvacuationSessionStatus.PAUSED)
        ).map(EvacuationSessionResponse::fromEntity).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public EvacuationSessionResponse getSession(String sessionId) {
        EvacuationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ServiceException.notFound("Evacuation session not found with ID: " + sessionId));
        return EvacuationSessionResponse.fromEntity(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvacuationSessionResponse> getBuildingSessions(String buildingId) {
        return sessionRepository.findByBuildingIdOrderByCreatedAtDesc(buildingId).stream()
                .map(EvacuationSessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingEmergencyStatusResponse getBuildingEmergencyStatus(String buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        List<IncidentResponse> activeIncidents = incidentService.getActiveBuildingIncidents(buildingId);
        long criticalCount = activeIncidents.stream()
                .filter(i -> i.severity() == IncidentSeverity.CRITICAL)
                .count();

        EvacuationSessionResponse activeSession = getActiveSession(buildingId);

        boolean emergencyActive = (activeSession != null && activeSession.sessionStatus() == EvacuationSessionStatus.ACTIVE)
                || criticalCount > 0
                || activeIncidents.stream().anyMatch(i -> i.severity() == IncidentSeverity.HIGH);

        int occupantsAtRisk = 0;
        int clearedRooms = 0;
        int evacuatingRooms = 0;

        if (activeSession != null) {
            occupantsAtRisk = activeSession.remainingOccupants();
            clearedRooms = activeSession.clearedRooms();
            evacuatingRooms = activeSession.evacuatingRooms();
        } else {
            List<Room> rooms = roomRepository.findByFloorBuildingId(buildingId);
            for (Room r : rooms) {
                occupantsAtRisk += Math.max(0, r.getCurrentOccupancy());
            }
        }

        List<EvacuationNode> exits = nodeRepository.findByBuildingIdAndNodeType(buildingId, EvacuationNodeType.EMERGENCY_EXIT);
        long safeExits = exits.stream().filter(e -> !e.isBlocked() && e.isSafe()).count();

        List<EvacuationRoute> routes = routeRepository.findByBuildingId(buildingId);
        long blockedRoutes = routes.stream().filter(r -> r.getRouteStatus() == RouteStatus.NO_SAFE_ROUTE).count();

        return new BuildingEmergencyStatusResponse(
                building.getId(),
                building.getName(),
                emergencyActive,
                activeIncidents.size(),
                criticalCount,
                activeSession,
                occupantsAtRisk,
                clearedRooms,
                evacuatingRooms,
                (int) safeExits,
                (int) blockedRoutes,
                activeIncidents
        );
    }

    @Override
    public BuildingEmergencyStatusResponse activateBuildingEmergency(String buildingId, String reason,
                                                                     String userId, String userName) {
        CreateIncidentRequest incReq = new CreateIncidentRequest();
        incReq.setBuildingId(buildingId);
        incReq.setCategory(IncidentCategory.MANUAL_EMERGENCY);
        incReq.setSeverity(IncidentSeverity.CRITICAL);
        incReq.setDetectionSource(IncidentSource.MANUAL);
        incReq.setTitle("General Building Emergency Declared");
        incReq.setDescription(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Emergency protocol triggered from Command Center");

        IncidentResponse incident = incidentService.createIncident(incReq, userId, userName);

        // Start evacuation if no active session
        EvacuationSessionResponse activeSession = getActiveSession(buildingId);
        if (activeSession == null) {
            StartEvacuationRequest evacReq = new StartEvacuationRequest();
            evacReq.setIncidentId(incident.id());
            evacReq.setSessionName("Evacuation: " + incident.title());
            evacReq.setNotes(incident.description());
            startEvacuation(buildingId, evacReq, userId, userName);
        }

        BuildingEmergencyStatusResponse status = getBuildingEmergencyStatus(buildingId);
        broadcaster.broadcastEmergencyStatus(buildingId, status);
        return status;
    }
}
