package com.smartbuilding.emergency.model;

import com.smartbuilding.building.model.Building;
import com.smartbuilding.building.model.Room;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evacuation_routes")
public class EvacuationRoute {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private EvacuationSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_room_id", nullable = false)
    private Room originRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_node_id", nullable = false)
    private EvacuationNode originNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_exit_id")
    private EvacuationNode destinationExit;

    @Column(name = "destination_exit_name", length = 128)
    private String destinationExitName;

    @Column(name = "ordered_node_ids", columnDefinition = "TEXT", nullable = false)
    private String orderedNodeIds;

    @Column(name = "ordered_coordinates", columnDefinition = "TEXT", nullable = false)
    private String orderedCoordinates;

    @Column(name = "total_distance_meters", nullable = false)
    private double totalDistanceMeters = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_status", length = 32, nullable = false)
    private RouteStatus routeStatus = RouteStatus.SAFE;

    @Column(name = "floor_transitions", columnDefinition = "TEXT")
    private String floorTransitions;

    @Column(name = "calculation_reason", columnDefinition = "TEXT")
    private String calculationReason;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    public EvacuationRoute() {}

    public EvacuationRoute(String id, EvacuationSession session, Building building, Room originRoom,
                           EvacuationNode originNode, EvacuationNode destinationExit,
                           String destinationExitName, String orderedNodeIds, String orderedCoordinates,
                           double totalDistanceMeters, RouteStatus routeStatus,
                           String floorTransitions, String calculationReason) {
        this.id = id;
        this.session = session;
        this.building = building;
        this.originRoom = originRoom;
        this.originNode = originNode;
        this.destinationExit = destinationExit;
        this.destinationExitName = destinationExitName;
        this.orderedNodeIds = orderedNodeIds;
        this.orderedCoordinates = orderedCoordinates;
        this.totalDistanceMeters = totalDistanceMeters;
        this.routeStatus = routeStatus != null ? routeStatus : RouteStatus.SAFE;
        this.floorTransitions = floorTransitions;
        this.calculationReason = calculationReason;
        this.calculatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public EvacuationSession getSession() { return session; }
    public void setSession(EvacuationSession session) { this.session = session; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public Room getOriginRoom() { return originRoom; }
    public void setOriginRoom(Room originRoom) { this.originRoom = originRoom; }

    public EvacuationNode getOriginNode() { return originNode; }
    public void setOriginNode(EvacuationNode originNode) { this.originNode = originNode; }

    public EvacuationNode getDestinationExit() { return destinationExit; }
    public void setDestinationExit(EvacuationNode destinationExit) { this.destinationExit = destinationExit; }

    public String getDestinationExitName() { return destinationExitName; }
    public void setDestinationExitName(String destinationExitName) { this.destinationExitName = destinationExitName; }

    public String getOrderedNodeIds() { return orderedNodeIds; }
    public void setOrderedNodeIds(String orderedNodeIds) { this.orderedNodeIds = orderedNodeIds; }

    public String getOrderedCoordinates() { return orderedCoordinates; }
    public void setOrderedCoordinates(String orderedCoordinates) { this.orderedCoordinates = orderedCoordinates; }

    public double getTotalDistanceMeters() { return totalDistanceMeters; }
    public void setTotalDistanceMeters(double totalDistanceMeters) { this.totalDistanceMeters = totalDistanceMeters; }

    public RouteStatus getRouteStatus() { return routeStatus; }
    public void setRouteStatus(RouteStatus routeStatus) { this.routeStatus = routeStatus; }

    public String getFloorTransitions() { return floorTransitions; }
    public void setFloorTransitions(String floorTransitions) { this.floorTransitions = floorTransitions; }

    public String getCalculationReason() { return calculationReason; }
    public void setCalculationReason(String calculationReason) { this.calculationReason = calculationReason; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
