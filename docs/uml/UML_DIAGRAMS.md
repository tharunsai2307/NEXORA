# NEXORA — Unified Modeling Language (UML) Specifications

**System:** NEXORA — Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Document:** Object-Oriented Analysis and Design (OOAD) UML Models  
**Specification Level:** Implementation-Grounded (Reflects Active Phases 1 - 8 Codebase)

---

## 1. UML Use Case Diagram

```mermaid
flowchart LR
    %% Actors
    Admin(["System Administrator"])
    Manager(["Building Manager"])
    Safety(["Safety Officer"])
    Occupant(["Building Occupant"])

    subgraph NEXORASystem ["NEXORA Platform Scope"]
        subgraph AuthScope ["Authentication & Administration"]
            UC1["Authenticate Session\n(JWT Login / Register)"]
            UC2["Manage Users & Roles\n(RBAC Provisioning)"]
            UC3["Configure Building Fleet\n(Buildings, Floors, Rooms)"]
        end

        subgraph TelemetryScope ["IoT Simulation & Telemetry"]
            UC4["Manage Virtual Sensors\n(Create / Configure Sensors)"]
            UC5["Execute Simulation Sessions\n(Run / Pause / Stop Scenarios)"]
            UC6["Inspect Live Readings\n(Telemetry Graphs & Stats)"]
        end

        subgraph AiScope ["Edge AI Intelligence"]
            UC7["Monitor Multi-Modal Anomalies\n(Isolation Forest Inference)"]
            UC8["View Occupancy Forecasts\n(15-Minute ML Predictions)"]
            UC9["Evaluate Room Safety Risks\n(Explainable Threat Scores)"]
            UC10["Calibrate / Retrain Models\n(Trigger Synthetic Calibration)"]
        end

        subgraph TwinScope ["3D Digital Twin"]
            UC11["Interact with 3D Twin\n(Orbit, Explode, Wireframe)"]
            UC12["Inspect Room State\n(Telemetry & Risk Heatmaps)"]
        end

        subgraph EmergencyScope ["Emergency & Evacuation"]
            UC13["Declare Emergency Incident\n(Manual or AI-Triggered)"]
            UC14["Compute Evacuation Routes\n(Weighted Dijkstra Pathfinding)"]
            UC15["Toggle Corridor Blockades\n(Simulate Obstacles & Reroute)"]
            UC16["Track Evacuation Clearance\n(Room Occupancy Depletion)"]
        end

        subgraph AnalyticsScope ["Analytics & Post-Mortem"]
            UC17["Query Telemetry Trends\n(Historical Heatmaps & Bars)"]
            UC18["Author Incident Post-Mortem\n(Draft, Review, Finalize)"]
            UC19["Download Executive PDF Report\n(Incident / Fleet Reports)"]
        end
    end

    %% Administrator Associations
    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4
    Admin --> UC5
    Admin --> UC10
    Admin --> UC18
    Admin --> UC19

    %% Building Manager Associations
    Manager --> UC1
    Manager --> UC3
    Manager --> UC4
    Manager --> UC5
    Manager --> UC6
    Manager --> UC8
    Manager --> UC11
    Manager --> UC17
    Manager --> UC19

    %% Safety Officer Associations
    Safety --> UC1
    Safety --> UC6
    Safety --> UC7
    Safety --> UC9
    Safety --> UC11
    Safety --> UC12
    Safety --> UC13
    Safety --> UC14
    Safety --> UC15
    Safety --> UC16
    Safety --> UC18
    Safety --> UC19

    %% Occupant Associations
    Occupant --> UC1
    Occupant --> UC11
    Occupant --> UC14
```

---

## 2. UML Domain Class Diagram

```mermaid
classDiagram
    direction TB

    class User {
        +String id
        +String email
        +String passwordHash
        +String fullName
        +Role role
        +UserStatus status
        +Instant createdAt
        +Instant updatedAt
    }

    class Building {
        +String id
        +String name
        +String buildingCode
        +String address
        +BuildingType buildingType
        +BuildingStatus status
        +int totalFloors
        +double totalAreaSqFt
        +int totalCapacity
    }

    class Floor {
        +String id
        +String floorName
        +int floorNumber
        +double floorAreaSqFt
        +int roomCount
        +FloorStatus status
    }

    class Room {
        +String id
        +String roomNumber
        +String roomName
        +RoomType roomType
        +int maximumCapacity
        +int currentOccupancy
        +double areaSqFt
        +double positionX
        +double positionY
        +double positionZ
        +double dimensionWidth
        +double dimensionHeight
        +double dimensionDepth
        +SafetyStatus safetyStatus
    }

    class Sensor {
        +String id
        +String sensorCode
        +String sensorName
        +SensorType sensorType
        +SensorStatus status
        +String unit
        +int simulationIntervalSeconds
        +double currentReading
        +Instant lastReadingAt
    }

    class SensorReading {
        +Long id
        +double readingValue
        +Instant recordedAt
        +String qualityFlag
    }

    class AiPrediction {
        +String id
        +PredictionType predictionType
        +Double predictedValue
        +Double confidenceScore
        +Instant predictionTimestamp
        +Instant targetTimestamp
        +String explanationJson
    }

    class RoomRiskAssessment {
        +String id
        +RiskLevel riskLevel
        +double overallRiskScore
        +double environmentalRiskScore
        +double occupancyRiskScore
        +double anomalyFactor
        +boolean criticalOverride
        +Instant assessedAt
    }

    class Incident {
        +String id
        +String referenceNumber
        +IncidentCategory category
        +IncidentSeverity severity
        +IncidentStatus status
        +IncidentSource detectionSource
        +String title
        +String description
        +Instant detectedAt
        +Instant acknowledgedAt
        +Instant resolvedAt
        +Instant closedAt
    }

    class EmergencyAlert {
        +String id
        +String alertType
        +IncidentSeverity severity
        +String alertMessage
        +Instant broadcastAt
    }

    class EvacuationNode {
        +String id
        +String nodeName
        +String nodeCode
        +EvacuationNodeType nodeType
        +double posX
        +double posY
        +double posZ
        +boolean isBlocked
        +boolean isSafe
    }

    class EvacuationConnection {
        +String id
        +double distanceMeters
        +boolean isBidirectional
        +boolean isBlocked
        +boolean isAccessible
        +HazardLevel hazardLevel
    }

    class EvacuationRoute {
        +String id
        +String targetExitName
        +String pathNodeIdsJson
        +String pathCoordinatesJson
        +double totalDistanceMeters
        +RouteStatus status
        +String instructions
    }

    class IncidentPostMortem {
        +String id
        +String reportTitle
        +String reportStatus
        +String executiveSummary
        +String incidentOverview
        +String timelineNarrative
        +String sensorAnalysis
        +String aiAssessmentSummary
        +String rootCauseAssessment
        +String correctiveActions
        +String preventiveRecommendations
        +Instant finalizedAt
    }

    class GeneratedReport {
        +String id
        +String reportType
        +String reportName
        +String reportFormat
        +String generatedBy
        +Instant createdAt
    }

    %% Relationships
    Building "1" *-- "1..*" Floor : contains
    Floor "1" *-- "1..*" Room : divides into
    Room "1" *-- "0..*" Sensor : houses
    Sensor "1" *-- "0..*" SensorReading : generates
    Room "1" *-- "0..*" AiPrediction : forecasted by
    Room "1" *-- "0..*" RoomRiskAssessment : scored by

    Building "1" *-- "0..*" Incident : registers
    Incident "1" o-- "0..1" Room : originates in
    Incident "1" *-- "0..*" EmergencyAlert : generates
    Incident "1" o-- "0..1" User : assigned to
    Incident "1" *-- "0..1" IncidentPostMortem : documented by

    Building "1" *-- "0..*" EvacuationNode : maps
    EvacuationNode "1" -- "0..*" EvacuationConnection : connects from
    EvacuationNode "1" -- "0..*" EvacuationConnection : connects to
    Building "1" *-- "0..*" EvacuationRoute : provides
    EvacuationRoute "1" o-- "1" Room : egress from
    EvacuationRoute "1" o-- "1" EvacuationNode : egress to
    Building "1" *-- "0..*" GeneratedReport : cataloged in
```

---

## 3. Sequence Diagram 1: Virtual IoT Fleet Telemetry & Edge AI Anomaly Loop

```mermaid
sequenceDiagram
    autonumber
    actor Officer as Safety Officer
    participant SimEngine as SimulationEngineServiceImpl
    participant SensorRepo as SensorReadingRepository
    participant AiClient as AiServiceClientImpl
    participant FastAPI as Python AI Engine (FastAPI)
    participant Broadcaster as SensorRealtimeBroadcaster
    participant STOMP as WebSocket STOMP Broker
    participant WebClient as Frontend Digital Twin

    Note over Officer,WebClient: Scheduled Simulation Interval (every 5-10s)
    SimEngine->>SimEngine: ScheduledWorker.tick()
    activate SimEngine

    SimEngine->>SensorRepo: saveBatch(generatedReadings)
    SimEngine->>Broadcaster: broadcastBatchReadings(buildingId, readings)
    Broadcaster->>STOMP: /topic/buildings/{id}/sensors/batch
    STOMP-->>WebClient: STOMP Frame (Live Sensor Readings)

    Note over SimEngine,FastAPI: Asynchronous AI Inference Dispatch
    SimEngine->>AiClient: detectAnomaly(roomTelemetryPayload)
    activate AiClient
    AiClient->>FastAPI: POST /api/v1/inference/anomaly
    activate FastAPI
    FastAPI->>FastAPI: IsolationForest.score_samples()
    FastAPI-->>AiClient: 200 OK (anomalyScore, severity, contributingFeatures)
    deactivate FastAPI
    AiClient-->>SimEngine: AnomalyDetectionResult
    deactivate AiClient

    alt Anomaly Detected (Severity >= HIGH)
        SimEngine->>Broadcaster: broadcastAiAnomaly(buildingId, anomalyPayload)
        Broadcaster->>STOMP: /topic/buildings/{id}/ai/anomalies
        STOMP-->>WebClient: STOMP Frame (Anomaly Alert)
        WebClient->>WebClient: Pulse Red Mesh on 3D Digital Twin
    end
    deactivate SimEngine
```

---

## 4. Sequence Diagram 2: Emergency Detection, Weighted Dijkstra Egress & Corridor Reroute

```mermaid
sequenceDiagram
    autonumber
    actor Officer as Safety Officer
    participant EOC as EmergencyPage UI
    participant IncSvc as IncidentServiceImpl
    participant EvacSvc as EvacuationRoutingServiceImpl
    participant Broadcaster as EmergencyRealtimeBroadcaster
    participant STOMP as WebSocket STOMP Broker
    participant TwinUI as 3D Digital Twin View

    %% 1. Emergency Declaration
    Officer->>EOC: Declares Hazard / Fire Incident in Room 104
    EOC->>IncSvc: POST /api/v1/incidents
    activate IncSvc
    IncSvc->>IncSvc: Save Incident (Severity: HIGH, Category: FIRE)
    IncSvc->>Broadcaster: broadcastIncidentUpdate(buildingId, incident)
    Broadcaster->>STOMP: /topic/buildings/{id}/incidents
    STOMP-->>TwinUI: STOMP Frame (Room 104 in Hazard State)
    deactivate IncSvc

    %% 2. Evacuation Calculation
    Officer->>EOC: Initiate Building Evacuation Protocol
    EOC->>EvacSvc: POST /api/v1/evacuation/calculate
    activate EvacSvc
    EvacSvc->>EvacSvc: Load Graph Nodes & Connections
    EvacSvc->>EvacSvc: Apply Hazard Avoidance Weight Penalty (+50m / Critical severed)
    EvacSvc->>EvacSvc: Execute Dijkstra Shortest Path to Safe Exits
    EvacSvc->>EvacSvc: Persist Calculated Routes
    EvacSvc->>Broadcaster: broadcastRoutesUpdate(buildingId, routes)
    Broadcaster->>STOMP: /topic/buildings/{id}/routes
    STOMP-->>TwinUI: STOMP Frame (3D Polyline Waypoints)
    TwinUI->>TwinUI: Renders Glowing Green/Cyan Escape Vectors
    deactivate EvacSvc

    %% 3. Dynamic Obstacle / Corridor Blockade
    Officer->>EOC: Toggles Corridor Blockade on Stairwell A
    EOC->>EvacSvc: POST /api/v1/evacuation/connections/{id}/toggle-block
    activate EvacSvc
    EvacSvc->>EvacSvc: conn.setBlocked(true)
    EvacSvc->>EvacSvc: Trigger Immediate Dynamic Recalculation
    EvacSvc->>Broadcaster: broadcastRoutesUpdate(buildingId, newRoutes)
    Broadcaster->>STOMP: /topic/buildings/{id}/routes
    STOMP-->>TwinUI: Dynamic Path Shift to East Fire Escape
    deactivate EvacSvc
```

---

## 5. Sequence Diagram 3: Incident Post-Mortem & PDF Executive Report Generation

```mermaid
sequenceDiagram
    autonumber
    actor Manager as Building Manager
    participant UI as Analytics & Post-Mortem Studio
    participant PostMortemSvc as PostMortemServiceImpl
    participant AggSvc as AnalyticsAggregationServiceImpl
    participant PdfGen as PdfReportGenerator
    participant DB as PostgreSQL Database

    Manager->>UI: Selects Resolved Incident & Clicks "Generate Post-Mortem"
    UI->>PostMortemSvc: POST /api/v1/analytics/post-mortem/generate
    activate PostMortemSvc
    PostMortemSvc->>DB: Fetch Incident, Room, Timeline & Egress Logs
    PostMortemSvc->>AggSvc: Compile Room Sensor Min/Max/Avg & AI Scores
    PostMortemSvc->>PostMortemSvc: Synthesize Incident Narrative & Root Cause Draft
    PostMortemSvc->>DB: Save IncidentPostMortem (Status: DRAFT)
    PostMortemSvc-->>UI: 200 OK (PostMortemDto)
    deactivate PostMortemSvc

    Manager->>UI: Reviews Narrative & Clicks "Finalize Post-Mortem"
    UI->>PostMortemSvc: PUT /api/v1/analytics/post-mortem/{id} (Status: FINALIZED)
    activate PostMortemSvc
    PostMortemSvc->>DB: Update Status to FINALIZED, reviewedAt = now()
    PostMortemSvc-->>UI: 200 OK (Finalized PostMortemDto)
    deactivate PostMortemSvc

    Manager->>UI: Clicks "Download Formal PDF Report"
    UI->>PdfGen: GET /api/v1/reports/incident/{id}/download?format=PDF
    activate PdfGen
    PdfGen->>PdfGen: Stream Binary PDF Document
    PdfGen-->>UI: 200 OK (application/pdf binary stream)
    deactivate PdfGen
    UI-->>Manager: Browser Prompts File Save (NEXORA_Incident_Report.pdf)
```

---

## 6. UML Activity Diagram: Emergency Evacuation Lifecycle

```mermaid
stateDiagram-v2
    [*] --> NormalOperations: Platform Monitoring Idle

    state NormalOperations {
        TelemetryIngestion --> AnomalyScoring
        AnomalyScoring --> TelemetryIngestion
    }

    NormalOperations --> ThreatDetected: Environmental Spike / Smoke / Manual Alert

    state ThreatDetected {
        AssessSeverity --> SeverityCheck
        state SeverityCheck <<choice>>
        SeverityCheck --> WarningLog: Severity LOW / MEDIUM
        SeverityCheck --> EmergencyEscalation: Severity HIGH / CRITICAL
    }

    WarningLog --> NormalOperations: Stabilized / Acknowledged

    state EmergencyEscalation {
        TriggerAlarms: Dispatch STOMP Broadcast to Building Occupants
        IdentifyDangerZones: Mark Compromised Rooms & Sever Impassable Edges
        RunDijkstraRouting: Compute Shortest Clear Paths to Emergency Exits
        Stream3DWaypoints: Render Glowing Polyline Vectors in 3D Digital Twin
    }

    EmergencyEscalation --> ActiveEvacuation

    state ActiveEvacuation {
        TrackOccupantClearing: Decrement Room Occupancy Counters
        CorridorChangeMonitor: Detect Corridor Blockades or Heat Spread
        state CorridorChangeCheck <<choice>>
        CorridorChangeCheck --> DynamicReroute: Egress Edge Blocked
        CorridorChangeCheck --> ContinueEgress: Path Remains Clear
        DynamicReroute --> Stream3DWaypoints: Update 3D Paths Instantly
        ContinueEgress --> TrackOccupantClearing
    }

    ActiveEvacuation --> AllZonesCleared: Building Occupancy == 0

    state AllZonesCleared {
        DeactivateEmergency: Transition Incident to RESOLVED
        ArchiveEgressMetrics: Record Evacuation Duration & Route Efficiency
        TriggerPostMortemStudio: Author Incident Retrospective & Action Items
    }

    AllZonesCleared --> NormalOperations: System Reset to Standby
```
