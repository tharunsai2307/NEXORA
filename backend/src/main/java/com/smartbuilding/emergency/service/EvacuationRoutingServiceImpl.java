package com.smartbuilding.emergency.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Floor;
import com.smartbuilding.building.model.Room;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.FloorRepository;
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
public class EvacuationRoutingServiceImpl implements EvacuationRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(EvacuationRoutingServiceImpl.class);

    private final EvacuationNodeRepository nodeRepository;
    private final EvacuationConnectionRepository connectionRepository;
    private final EvacuationRouteRepository routeRepository;
    private final EvacuationSessionRepository sessionRepository;
    private final IncidentRepository incidentRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final EmergencyRealtimeBroadcaster broadcaster;
    private final ObjectMapper objectMapper;

    public EvacuationRoutingServiceImpl(EvacuationNodeRepository nodeRepository,
                                       EvacuationConnectionRepository connectionRepository,
                                       EvacuationRouteRepository routeRepository,
                                       EvacuationSessionRepository sessionRepository,
                                       IncidentRepository incidentRepository,
                                       BuildingRepository buildingRepository,
                                       FloorRepository floorRepository,
                                       RoomRepository roomRepository,
                                       EmergencyRealtimeBroadcaster broadcaster,
                                       ObjectMapper objectMapper) {
        this.nodeRepository = nodeRepository;
        this.connectionRepository = connectionRepository;
        this.routeRepository = routeRepository;
        this.sessionRepository = sessionRepository;
        this.incidentRepository = incidentRepository;
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.broadcaster = broadcaster;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EvacuationRouteResponse> calculateRoutesForBuilding(String buildingId, String sessionId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        EvacuationSession session = null;
        if (sessionId != null) {
            session = sessionRepository.findById(sessionId).orElse(null);
        }

        List<EvacuationNode> allNodes = nodeRepository.findByBuildingId(buildingId);
        if (allNodes.isEmpty()) {
            logger.info("No evacuation graph found for building {}. Auto-initializing default graph...", buildingId);
            initializeDefaultGraph(buildingId);
            allNodes = nodeRepository.findByBuildingId(buildingId);
        }

        List<EvacuationConnection> allConnections = connectionRepository.findByBuildingId(buildingId);

        // Identify hazardous / danger rooms from active incidents
        List<Incident> activeIncidents = incidentRepository.findByBuildingIdAndStatusInOrderByDetectedAtDesc(
                buildingId,
                List.of(IncidentStatus.DETECTED, IncidentStatus.ACTIVE, IncidentStatus.ACKNOWLEDGED, IncidentStatus.RESPONDING)
        );

        Set<String> dangerRoomIds = new HashSet<>();
        for (Incident inc : activeIncidents) {
            if (inc.getRoom() != null && (inc.getSeverity() == IncidentSeverity.HIGH || inc.getSeverity() == IncidentSeverity.CRITICAL)) {
                dangerRoomIds.add(inc.getRoom().getId());
            }
        }

        // Build adjacency map for Dijkstra
        Map<String, List<Edge>> adjacency = new HashMap<>();
        for (EvacuationNode n : allNodes) {
            adjacency.put(n.getId(), new ArrayList<>());
        }

        for (EvacuationConnection conn : allConnections) {
            if (conn.isBlocked() || !conn.isAccessible()) {
                continue; // Skip physically blocked edges
            }
            EvacuationNode u = conn.getFromNode();
            EvacuationNode v = conn.getToNode();

            double weight = conn.getDistanceMeters();
            if (conn.getHazardLevel() == HazardLevel.HIGH) {
                weight += 50.0; // Penalty
            } else if (conn.getHazardLevel() == HazardLevel.CRITICAL) {
                continue; // Impassable
            }

            adjacency.get(u.getId()).add(new Edge(v.getId(), weight, conn));
            if (conn.isBidirectional()) {
                adjacency.get(v.getId()).add(new Edge(u.getId(), weight, conn));
            }
        }

        // Identify target exit nodes
        List<EvacuationNode> exitNodes = allNodes.stream()
                .filter(n -> n.getNodeType() == EvacuationNodeType.EMERGENCY_EXIT && !n.isBlocked() && n.isSafe())
                .collect(Collectors.toList());

        Map<String, EvacuationNode> nodeLookup = allNodes.stream()
                .collect(Collectors.toMap(EvacuationNode::getId, n -> n));

        // Find all room nodes
        List<EvacuationNode> roomNodes = allNodes.stream()
                .filter(n -> n.getNodeType() == EvacuationNodeType.ROOM && n.getRoom() != null)
                .collect(Collectors.toList());

        List<EvacuationRoute> calculatedRoutes = new ArrayList<>();

        for (EvacuationNode originNode : roomNodes) {
            Room originRoom = originNode.getRoom();
            boolean originInDanger = dangerRoomIds.contains(originRoom.getId());

            // Run Dijkstra from originNode to all reachable nodes
            DijkstraResult result = runDijkstra(originNode.getId(), adjacency, dangerRoomIds, nodeLookup, originNode.getId());

            // Find best safe exit
            EvacuationNode bestExit = null;
            double minDistance = Double.MAX_VALUE;

            for (EvacuationNode exit : exitNodes) {
                Double dist = result.distances.get(exit.getId());
                if (dist != null && dist < minDistance) {
                    minDistance = dist;
                    bestExit = exit;
                }
            }

            String routeId = UUID.randomUUID().toString();
            if (bestExit != null) {
                // Reconstruct path
                List<String> pathNodeIds = reconstructPath(originNode.getId(), bestExit.getId(), result.previous);
                List<RouteNodeDto> waypoints = new ArrayList<>();
                List<double[]> coords = new ArrayList<>();
                StringBuilder floorTransitions = new StringBuilder();
                int prevFloor = -999;

                for (String nid : pathNodeIds) {
                    EvacuationNode nd = nodeLookup.get(nid);
                    int fn = nd.getFloor() != null ? nd.getFloor().getFloorNumber() : 0;
                    waypoints.add(new RouteNodeDto(
                            nd.getId(),
                            nd.getNodeName(),
                            nd.getNodeType(),
                            fn,
                            nd.getPosX(),
                            nd.getPosY(),
                            nd.getPosZ()
                    ));
                    coords.add(new double[]{nd.getPosX(), nd.getPosY(), nd.getPosZ()});

                    if (prevFloor != -999 && prevFloor != fn) {
                        if (floorTransitions.length() > 0) floorTransitions.append(" -> ");
                        floorTransitions.append("Level ").append(prevFloor).append(" to Level ").append(fn);
                    }
                    prevFloor = fn;
                }

                String nodeIdsJson = "[]";
                String coordsJson = "[]";
                try {
                    nodeIdsJson = objectMapper.writeValueAsString(pathNodeIds);
                    coordsJson = objectMapper.writeValueAsString(coords);
                } catch (Exception ignored) {}

                RouteStatus status = originInDanger ? RouteStatus.CAUTION : RouteStatus.SAFE;
                String reason = originInDanger
                        ? "Active hazard reported in origin room; urgent primary evacuation path computed to " + bestExit.getNodeName()
                        : "Optimal clear path via egress corridors and stairways to " + bestExit.getNodeName();

                EvacuationRoute route = new EvacuationRoute(
                        routeId,
                        session,
                        building,
                        originRoom,
                        originNode,
                        bestExit,
                        bestExit.getNodeName(),
                        nodeIdsJson,
                        coordsJson,
                        Math.round(minDistance * 10.0) / 10.0,
                        status,
                        floorTransitions.toString(),
                        reason
                );
                calculatedRoutes.add(route);
            } else {
                // No safe route available!
                String reason = "No safe egress path found: all corridors or stairwells to emergency exits are compromised or blocked.";
                EvacuationRoute route = new EvacuationRoute(
                        routeId,
                        session,
                        building,
                        originRoom,
                        originNode,
                        null,
                        "NONE",
                        "[]",
                        "[]",
                        0.0,
                        RouteStatus.NO_SAFE_ROUTE,
                        "",
                        reason
                );
                calculatedRoutes.add(route);
            }
        }

        // Persist routes
        if (session != null) {
            routeRepository.deleteBySessionId(session.getId());
        } else {
            routeRepository.deleteByBuildingId(buildingId);
        }
        List<EvacuationRoute> saved = routeRepository.saveAll(calculatedRoutes);

        List<EvacuationRouteResponse> responses = saved.stream()
                .map(this::mapToRouteResponse)
                .collect(Collectors.toList());

        broadcaster.broadcastRoutesUpdate(buildingId, responses);
        logger.info("Calculated {} evacuation routes for building {}", responses.size(), building.getName());
        return responses;
    }

    @Override
    @Transactional
    public List<EvacuationRouteResponse> getBuildingRoutes(String buildingId) {
        List<EvacuationRoute> routes = routeRepository.findByBuildingId(buildingId);
        if (routes.isEmpty()) {
            return calculateRoutesForBuilding(buildingId, null);
        }
        return routes.stream().map(this::mapToRouteResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvacuationRouteResponse> getSessionRoutes(String sessionId) {
        return routeRepository.findBySessionId(sessionId).stream()
                .map(this::mapToRouteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EvacuationGraphResponse getBuildingGraph(String buildingId) {
        List<EvacuationNodeDto> nodes = nodeRepository.findByBuildingId(buildingId).stream()
                .map(EvacuationNodeDto::fromEntity)
                .collect(Collectors.toList());
        List<EvacuationConnectionDto> connections = connectionRepository.findByBuildingId(buildingId).stream()
                .map(EvacuationConnectionDto::fromEntity)
                .collect(Collectors.toList());
        return new EvacuationGraphResponse(buildingId, nodes, connections);
    }

    @Override
    public EvacuationConnectionDto toggleConnectionBlock(String connectionId) {
        EvacuationConnection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> ServiceException.notFound("Evacuation connection not found with ID: " + connectionId));

        boolean newBlocked = !conn.isBlocked();
        conn.setBlocked(newBlocked);
        EvacuationConnection saved = connectionRepository.save(conn);

        // Recalculate routes immediately to reflect changed egress path
        calculateRoutesForBuilding(saved.getBuilding().getId(), null);

        logger.info("Connection {} block status changed to {}", connectionId, newBlocked);
        return EvacuationConnectionDto.fromEntity(saved);
    }

    @Override
    public EvacuationGraphResponse initializeDefaultGraph(String buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> ServiceException.notFound("Building not found with ID: " + buildingId));

        List<Floor> floors = floorRepository.findByBuildingIdOrderByFloorNumberAsc(buildingId);
        if (floors.isEmpty()) {
            return getBuildingGraph(buildingId);
        }

        final Floor effectiveGroundFloor = floors.stream()
                .filter(f -> f.getFloorNumber() == 0)
                .findFirst()
                .orElse(floors.get(0));

        // 1. Ensure exits exist on ground floor
        EvacuationNode mainExit = nodeRepository.findByBuildingIdAndNodeCode(buildingId, "NODE-EXIT-MAIN")
                .orElseGet(() -> nodeRepository.save(new EvacuationNode(
                        UUID.randomUUID().toString(), building, effectiveGroundFloor, null,
                        "North Emergency Egress Plaza", "NODE-EXIT-MAIN", EvacuationNodeType.EMERGENCY_EXIT,
                        0.0, 0.0, -18.0
                )));

        EvacuationNode eastExit = nodeRepository.findByBuildingIdAndNodeCode(buildingId, "NODE-EXIT-EAST")
                .orElseGet(() -> nodeRepository.save(new EvacuationNode(
                        UUID.randomUUID().toString(), building, effectiveGroundFloor, null,
                        "East Fire Escape Door", "NODE-EXIT-EAST", EvacuationNodeType.EMERGENCY_EXIT,
                        32.0, 0.0, -5.0
                )));

        // 2. Iterate each floor to create Corridor and Staircase nodes
        Map<Integer, EvacuationNode> stairsByFloor = new HashMap<>();
        Map<Integer, EvacuationNode> corridorsByFloor = new HashMap<>();

        for (Floor floor : floors) {
            int fn = floor.getFloorNumber();
            String stairCode = "NODE-L" + fn + "-STAIR";
            EvacuationNode stairNode = nodeRepository.findByBuildingIdAndNodeCode(buildingId, stairCode)
                    .orElseGet(() -> nodeRepository.save(new EvacuationNode(
                            UUID.randomUUID().toString(), building, floor, null,
                            "Stairwell A (Level " + fn + ")", stairCode, EvacuationNodeType.STAIRCASE,
                            -10.0, 0.0, -12.0
                    )));
            stairsByFloor.put(fn, stairNode);

            String corrCode = "NODE-L" + fn + "-CORR";
            EvacuationNode corrNode = nodeRepository.findByBuildingIdAndNodeCode(buildingId, corrCode)
                    .orElseGet(() -> nodeRepository.save(new EvacuationNode(
                            UUID.randomUUID().toString(), building, floor, null,
                            "Level " + fn + " Main Corridor", corrCode, EvacuationNodeType.CORRIDOR,
                            0.0, 0.0, -6.0
                    )));
            corridorsByFloor.put(fn, corrNode);

            // Connect corridor to stair
            ensureConnection(building, corrNode, stairNode, 12.0);

            // If ground floor, connect corridor to exits
            if (fn == effectiveGroundFloor.getFloorNumber()) {
                ensureConnection(building, corrNode, mainExit, 10.0);
                ensureConnection(building, corrNode, eastExit, 25.0);
            }

            // Create room nodes & connect to corridor
            List<Room> rooms = roomRepository.findByFloorIdOrderByRoomCodeAsc(floor.getId());
            for (Room room : rooms) {
                String roomNodeCode = "NODE-" + room.getRoomCode();
                EvacuationNode roomNode = nodeRepository.findByBuildingIdAndRoomId(buildingId, room.getId())
                        .orElseGet(() -> nodeRepository.save(new EvacuationNode(
                                UUID.randomUUID().toString(), building, floor, room,
                                room.getRoomName() + " Egress Node", roomNodeCode, EvacuationNodeType.ROOM,
                                room.getPosX(), room.getPosY(), room.getPosZ()
                        )));
                double dist = Math.max(5.0, Math.hypot(roomNode.getPosX() - corrNode.getPosX(), roomNode.getPosZ() - corrNode.getPosZ()));
                ensureConnection(building, roomNode, corrNode, Math.round(dist * 10.0) / 10.0);
            }
        }

        // 3. Connect stairwells vertically
        List<Integer> sortedFloorNums = new ArrayList<>(stairsByFloor.keySet());
        Collections.sort(sortedFloorNums);
        for (int i = 0; i < sortedFloorNums.size() - 1; i++) {
            EvacuationNode lowerStair = stairsByFloor.get(sortedFloorNums.get(i));
            EvacuationNode upperStair = stairsByFloor.get(sortedFloorNums.get(i + 1));
            ensureConnection(building, upperStair, lowerStair, 8.0);
        }

        logger.info("Initialized default evacuation graph for building {}", buildingId);
        return getBuildingGraph(buildingId);
    }

    private void ensureConnection(Building building, EvacuationNode from, EvacuationNode to, double dist) {
        List<EvacuationConnection> existing = connectionRepository.findConnectedEdges(building.getId(), from.getId());
        for (EvacuationConnection c : existing) {
            if ((c.getFromNode().getId().equals(from.getId()) && c.getToNode().getId().equals(to.getId())) ||
                (c.getFromNode().getId().equals(to.getId()) && c.getToNode().getId().equals(from.getId()))) {
                return; // already connected
            }
        }
        EvacuationConnection conn = new EvacuationConnection(
                UUID.randomUUID().toString(),
                building,
                from,
                to,
                dist,
                HazardLevel.NONE,
                false,
                true,
                true
        );
        connectionRepository.save(conn);
    }

    private DijkstraResult runDijkstra(String sourceNodeId, Map<String, List<Edge>> adjacency,
                                      Set<String> dangerRoomIds, Map<String, EvacuationNode> nodeLookup,
                                      String originRoomNodeId) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));

        distances.put(sourceNodeId, 0.0);
        pq.add(new NodeDistance(sourceNodeId, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            if (current.distance > distances.getOrDefault(current.nodeId, Double.MAX_VALUE)) {
                continue;
            }

            List<Edge> neighbors = adjacency.getOrDefault(current.nodeId, Collections.emptyList());
            for (Edge edge : neighbors) {
                // If neighbor is an intermediate room that is in danger (and not the origin itself), avoid it
                EvacuationNode targetNode = nodeLookup.get(edge.targetId);
                if (targetNode != null && targetNode.getNodeType() == EvacuationNodeType.ROOM && targetNode.getRoom() != null) {
                    if (!edge.targetId.equals(originRoomNodeId) && dangerRoomIds.contains(targetNode.getRoom().getId())) {
                        continue; // Do not path through active hazard rooms!
                    }
                }

                double newDist = current.distance + edge.weight;
                if (newDist < distances.getOrDefault(edge.targetId, Double.MAX_VALUE)) {
                    distances.put(edge.targetId, newDist);
                    previous.put(edge.targetId, current.nodeId);
                    pq.add(new NodeDistance(edge.targetId, newDist));
                }
            }
        }

        return new DijkstraResult(distances, previous);
    }

    private List<String> reconstructPath(String startId, String endId, Map<String, String> previous) {
        LinkedList<String> path = new LinkedList<>();
        String curr = endId;
        while (curr != null) {
            path.addFirst(curr);
            if (curr.equals(startId)) break;
            curr = previous.get(curr);
        }
        return path;
    }

    private EvacuationRouteResponse mapToRouteResponse(EvacuationRoute r) {
        List<RouteNodeDto> waypoints = new ArrayList<>();
        try {
            List<String> nodeIds = objectMapper.readValue(r.getOrderedNodeIds(), new TypeReference<List<String>>() {});
            for (String nid : nodeIds) {
                nodeRepository.findById(nid).ifPresent(nd -> {
                    int fn = nd.getFloor() != null ? nd.getFloor().getFloorNumber() : 0;
                    waypoints.add(new RouteNodeDto(
                            nd.getId(),
                            nd.getNodeName(),
                            nd.getNodeType(),
                            fn,
                            nd.getPosX(),
                            nd.getPosY(),
                            nd.getPosZ()
                    ));
                });
            }
        } catch (Exception ignored) {}

        return new EvacuationRouteResponse(
                r.getId(),
                r.getSession() != null ? r.getSession().getId() : null,
                r.getBuilding().getId(),
                r.getOriginRoom().getId(),
                r.getOriginRoom().getRoomName(),
                r.getOriginNode().getId(),
                r.getDestinationExit() != null ? r.getDestinationExit().getId() : null,
                r.getDestinationExitName(),
                waypoints,
                r.getTotalDistanceMeters(),
                r.getRouteStatus(),
                r.getFloorTransitions(),
                r.getCalculationReason(),
                r.getCalculatedAt()
        );
    }

    private static class Edge {
        final String targetId;
        final double weight;
        final EvacuationConnection connection;

        Edge(String targetId, double weight, EvacuationConnection connection) {
            this.targetId = targetId;
            this.weight = weight;
            this.connection = connection;
        }
    }

    private static class NodeDistance {
        final String nodeId;
        final double distance;

        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }

    private static class DijkstraResult {
        final Map<String, Double> distances;
        final Map<String, String> previous;

        DijkstraResult(Map<String, Double> distances, Map<String, String> previous) {
            this.distances = distances;
            this.previous = previous;
        }
    }
}
