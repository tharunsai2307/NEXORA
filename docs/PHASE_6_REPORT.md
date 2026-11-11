# NEXORA — Phase 6 Implementation Report
## Emergency Management & Dynamic Evacuation Routing

**Project:** Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Product:** NEXORA — *Intelligence Beyond Infrastructure*  
**Phase:** 6 of 7 (Emergency Management & Dynamic Evacuation Routing)  
**Status:** Successfully Implemented, Verified & Full-Stack Compiled  

---

## 1. Executive Summary

Phase 6 elevates NEXORA from an observational monitoring and digital twin platform into an **intelligent building emergency command and dynamic evacuation routing system**. The platform now actively detects hazardous events across virtual IoT sensors and Edge AI models, logs structured lifecycle incidents, calculates real-time safe multi-floor egress routes via Dijkstra's algorithm avoiding dynamic hazard zones, and renders real-time 3D evacuation corridors and status beacons directly within the 3D Digital Twin and the Emergency Operations Center.

---

## 2. Architecture & Subsystems

```
                                      +-----------------------------+
                                      |   Virtual IoT Sensors &     |
                                      |   Phase 4 Edge AI Models    |
                                      +--------------+--------------+
                                                     |
                                                     v (Threshold Evaluation & Deduplication)
                                      +-----------------------------+
                                      |  Incident Management Core   |
                                      |  (DETECTED -> RESPONDING -> |
                                      |   RESOLVED -> CLOSED)       |
                                      +--------------+--------------+
                                                     |
                         +---------------------------+---------------------------+
                         |                                                       |
                         v                                                       v
        +---------------------------------+                     +---------------------------------+
        |  Building Evacuation Graph      |                     |  Evacuation Session Engine      |
        |  - Rooms, Corridors, Stairs     |                     |  - Active Clearance Tracking    |
        |  - Ground Emergency Exits       |                     |  - Assisted Occupant Flags      |
        |  - Blockable Egress Edges       |                     |  - Step-by-Step Simulation      |
        +----------------+----------------+                     +----------------+----------------+
                         |                                                       |
                         +---------------------------+---------------------------+
                                                     |
                                                     v
                                      +-----------------------------+
                                      |   Dynamic Dijkstra Engine   |
                                      |   Multi-Exit / Hazard Avoid |
                                      +--------------+--------------+
                                                     |
                                                     v (WebSocket Broadcasts)
                         +---------------------------+---------------------------+
                         |                                                       |
                         v                                                       v
        +---------------------------------+                     +---------------------------------+
        |   3D Digital Twin Integration   |                     |   Emergency Operations Center   |
        |   - 3D Spline Egress Tubes      |                     |   - Threat Level KPI Ribbon     |
        |   - Hazard Beacons & Exit Signs |                     |   - Triage & Officer Dispatch   |
        |   - Room Clearance Highlights   |                     |   - Interactive Graph Blocker   |
        +---------------------------------+                     +---------------------------------+
```

---

## 3. Incident Management Subsystem

### 3.1 Incident Taxonomies
- **Categories**: `FIRE`, `SMOKE`, `HIGH_TEMPERATURE`, `POOR_AIR_QUALITY`, `OVERCROWDING`, `MANUAL_EMERGENCY`, `OTHER`
- **Severities**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- **Lifecycles**: `DETECTED` $\rightarrow$ `ACTIVE` $\rightarrow$ `ACKNOWLEDGED` $\rightarrow$ `RESPONDING` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`
- **Detection Sources**: `SENSOR`, `AI`, `MANUAL`

### 3.2 Automated Detection & Cooldown Deduplication
- **Sensor-Driven Triggers**:
  - Smoke $> 50\,\text{ppm}$ generates high/critical smoke incident.
  - Temperature $> 48^\circ\text{C}$ generates thermal anomaly incident.
  - Air Quality $> 250\,\text{AQI}$ generates hazardous air quality alert.
  - Room Occupancy $> 1.25 \times \text{Capacity}$ generates overcrowding incident.
- **Edge AI Triggers**:
  - AI Risk Assessment returning `CRITICAL` risk or overall score $\ge 75$ automatically creates an urgent emergency incident.
- **Deduplication Cooldown**:
  - In-memory tracker suppresses repetitive alerts for the same room and category within a 180-second window, preventing alert storms while capturing ongoing hazard updates.

---

## 4. Building Evacuation Graph & Dynamic Dijkstra Pathfinder

### 4.1 Evacuation Graph Model
- **EvacuationNode**:
  - `ROOM`: Egress entrance/doorway corresponding to room 3D coordinates.
  - `CORRIDOR`: Hallway intersection or spine waypoint.
  - `STAIRCASE`: Inter-floor vertical landing connecting level 0, level 1, level 2.
  - `EMERGENCY_EXIT`: Ground exterior egress portals (North Emergency Egress Plaza, East Fire Escape Door).
- **EvacuationConnection**:
  - Distance in meters, accessibility flag, hazard level (`NONE`, `LOW`, `HIGH`, `CRITICAL`), bidirectional indicator, and interactive `isBlocked` toggle.

### 4.2 Dynamic Dijkstra Algorithm
- **Hazard Avoidance**: Any intermediate room or connection flagged with an active `HIGH` or `CRITICAL` incident is treated as impassable ($\infty$ edge cost).
- **Multi-Exit Evaluation**: The pathfinder explores paths to all available emergency exits simultaneously, selecting the exit that minimizes total safe travel distance.
- **Multi-Floor Transition**: Downward vertical traversal via stairwell landings is automatically linked to ground floor concourses and exterior exits.
- **No Safe Route Reporting**: When corridors or fire doors are blocked, the engine marks the route status as `NO_SAFE_ROUTE` with an explicit reason rather than generating a misleading path.

---

## 5. Evacuation Management & Clearance Tracking

- **EvacuationSession**: Associates an evacuation session with an incident or manual command center declaration.
- **Room-Level Clearance Tracking**:
  - Tracks simulated room states: `NOT_STARTED`, `EVACUATING`, `CLEARED`, `BLOCKED`, `NEEDS_ASSISTANCE`.
  - Configurable simulation attribute: `occupantsNeedingAssistance` flag for mobility-impaired or trapped occupants.
- **Step-by-Step Simulation**:
  - Allows safety operators to advance evacuation ticks (`POST /api/v1/evacuations/{id}/step`), progressively simulating occupant egress and marking rooms cleared in real-time.

---

## 6. 3D Digital Twin Integration

- **Emergency Display Mode**: Added to the existing React Three Fiber Digital Twin alongside `DEFAULT`, `RISK`, `OCCUPANCY`, `TEMPERATURE`, `SMOKE`, `AIR_QUALITY`.
- **Egress Tube Geometry**: Renders 3D CatmullRom spline tubes in glowing cyan (`#06b6d4`) or amber (`#f59e0b`) elevated along waypoints to safe exits.
- **Incident & Exit Markers**:
  - Flashing red sphere beacons and 3D HTML alerts for active hazard rooms.
  - Glowing green cylindrical markers with 3D HTML exit badges for safe emergency doors.
  - Color-coded clearance materials for rooms (calm green for cleared, amber for evacuating, magenta for assistance).

---

## 7. Emergency Operations Center (EOC) Frontend

- **Command Hub Overview**: Global threat indicator, active emergency status, clearance progress bar, and digital twin shortcut.
- **Incident Queue**: Filterable and searchable incident list with triage actions (Acknowledge, Assign to Safety Officer, Resolve, Log Incident).
- **Evacuation Control Room**: Session runner, step-by-step simulation button, and room status clearance grid.
- **Egress Route Inspector**: Waypoint breadcrumb breakdown, floor transitions, and routing explanation.
- **Graph Hazard Simulator**: Interactive connection blocking to test dynamic route recalculation.

---

## 8. Verification & Build Status

- **Java 21 Backend**:
  ```
  [INFO] Compiling 202 source files with javac [debug parameters release 21] to target/classes
  [INFO] BUILD SUCCESS
  ```
- **React Frontend**:
  ```
  $ tsc -b && vite build
  ✓ 2254 modules transformed.
  dist/index.html                     0.91 kB │ gzip:   0.51 kB
  dist/assets/index-FiQ2M5CV.css     50.36 kB │ gzip:   8.71 kB
  dist/assets/index-BssThcDm.js   1,364.29 kB │ gzip: 357.11 kB
  ✓ built in 16.15s
  ```
- **Git Commit Target**: `feat(phase-6): implement emergency management and dynamic evacuation routing`
