# NEXORA — Capstone Final Defense Presentation
## Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System

**Tagline:** Intelligence Beyond Infrastructure.  
**Presented by:** Senior Software Engineering Capstone Team  
**Scope:** Exactly 6 Defense Slides (Implementation-Grounded)

---

### Slide 1: Problem Statement & Proposed Solution

#### The Challenge
- **Disconnected Building Systems:** Modern commercial facilities operate HVAC, fire detection, access control, and spatial allocation in fragmented data silos.
- **Static, Hazardous Evacuation:** Traditional egress protocols rely on static exit signs that fail to account for dynamic fire spread, potentially guiding occupants into compromised zones.
- **Operational Inefficiencies:** Facility managers lack low-latency, predictive occupancy insights to optimize space utilization and HVAC conditioning.
- **High Deployment Barriers:** Physical IoT deployment for testing and training drills is capital-intensive and disruptive.

#### The NEXORA Solution
- **Unified Smart Building Platform:** Monolithic integration of simulated IoT telemetry, edge AI inference, and interactive 3D visualization.
- **Software-Driven Virtual Fleet:** High-throughput virtual sensor fleet simulating realistic physics across five key environmental and spatial dimensions.
- **Edge AI & Explainable Threat Scoring:** Local machine learning microservice providing sub-50ms anomaly detection, 15-minute occupancy forecasting, and multi-criteria risk assessment.
- **Dynamic Egress Routing:** Real-time weighted Dijkstra pathfinding that avoids active hazards and dynamically recalculates when corridors are blocked.

---

### Slide 2: System Architecture & Technology Stack

#### 4-Tier Enterprise Architecture
1. **Presentation Tier:** React 18, TypeScript, Tailwind CSS, Three.js, React Three Fiber, Drei, Recharts (Served via Nginx Alpine on Port 80).
2. **Core Orchestration Tier:** Java 21 LTS, Spring Boot 3.3.5, Spring Security 6 (Stateless JWT), Spring Data JPA, Spring WebSocket (STOMP Broker).
3. **Edge Intelligence Tier:** Python 3.11, FastAPI, Uvicorn, Scikit-Learn 1.6, NumPy, Pandas (Sub-50ms Local Inference).
4. **Persistence Tier:** PostgreSQL 16 Alpine, Flyway Migration Engine (6 Versioned Migrations `V1` - `V6`), HikariCP Connection Pooling.

#### Architectural Guardrails
- **Frontend Isolation:** Client communicates strictly via REST (`/api/v1/*`) and STOMP (`/ws-raw`) through Nginx. Never directly touches PostgreSQL or Python.
- **AI Microservice Isolation:** Pure stateless intelligence service without direct database access; orchestrated exclusively by the Java backend.
- **Single DB Custodian:** PostgreSQL is exclusively managed by Spring Data JPA and Flyway versioned migrations.

---

### Slide 3: Core Modules & Key Innovations

#### 1. Topology & Role-Based Access Control (RBAC)
- Multi-building hierarchy (Building $\to$ Floor $\to$ Room) with 3D spatial bounding volumes `(posX, posY, posZ, width, depth, height)`.
- 4 Role Tiers: `SYSTEM_ADMIN`, `BUILDING_MANAGER`, `SAFETY_OFFICER`, `OCCUPANT`.

#### 2. Virtual IoT Sensor Fleet & Scenario Engine
- Concurrent background simulation engine with scheduled multi-threaded worker pools.
- 5 Sensor Categories: Temperature (°C), Humidity (%), Smoke (ppm), Air Quality (AQI), Occupancy (Count).
- Injectable scenarios: Normal Drift, Thermal Spike, Fire Hazard, High Occupancy, and Sensor Degradation.

#### 3. Incident Management & Automated Post-Mortem Studio
- Full incident lifecycle tracking: `DETECTED` $\to$ `ACKNOWLEDGED` $\to$ `RESPONDING` $\to$ `RESOLVED` $\to$ `CLOSED`.
- Post-Mortem Studio with automated AI narrative synthesis, sensor min/max/average statistics, and formal PDF generation.

---

### Slide 4: AI Intelligence & 3D Digital Twin

#### Edge Machine Learning Intelligence
- **Isolation Forest Multi-Modal Anomaly Detector:**
  - Evaluates 8-dimensional feature vectors (ambient readings + rolling deltas + trend velocity).
  - Normalizes raw decision scores to calibrated severity: `NORMAL`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- **Random Forest 15-Minute Occupancy Forecaster:**
  - 100-tree ensemble modeling diurnal cyclicity, room type, and capacity utilization ($R^2 = 0.935$, $\text{MAE} = 1.38$).
- **Explainable Multi-Criteria Safety Risk Engine:**
  - Weighted composite threat score with non-linear critical hazard overrides (Smoke $\ge 10.0\text{ ppm}$ or Temp $\ge 55^\circ\text{C}$).

#### Browser-Native 3D Digital Twin (Three.js / React Three Fiber)
- **Interactive Spatial Navigation:** Orbit controls with camera view presets (`TOP`, `FRONT`, `SIDE`, `RESET`).
- **Exploded View Assembly:** Real-time vertical floor separation slider ($0\text{m} - 15\text{m}$) for multi-story inspection.
- **Dynamic Visual Heatmaps:** Instant room color mapping based on telemetry, crowd density, and threat severity.
- **Resource Optimization:** Automatic geometry and WebGL material disposal to prevent context exhaustion.

---

### Slide 5: Emergency Management & Dynamic Evacuation

#### Dynamic Hazard-Avoidance Egress Algorithm
- Graph topology modeled with `ROOM`, `CORRIDOR`, `STAIRCASE`, and `EMERGENCY_EXIT` navigation nodes.
- **Weighted Dijkstra Formulation:**
  - Safe corridors: geometric distance in meters.
  - High hazard rooms / smoke plumes: **+50.0m distance penalty**.
  - Critical danger rooms / active fires: **Edge severed (impassable)**.
  - Multi-floor vertical transitions tracked across stairways to exterior ground plazas.

#### Real-Time Dynamic Rerouting Demo
1. Fire declared in Level 1 room (Room 104).
2. Primary corridor through Room 104 is severed.
3. Safety officer toggles blockade on Stairwell A.
4. Egress algorithm dynamically recalculates in $< 5\text{ ms}$.
5. WebSocket dispatches updated polyline waypoints to browser.
6. 3D Digital Twin shifts glowing escape vectors from North Plaza to East Fire Door instantly.

---

### Slide 6: Results, Limitations & Future Scope

#### Verified Performance Results
- **Python AI Inference Latency:** Mean $2.4\text{ ms}$ (Anomaly Detection), $3.8\text{ ms}$ (Occupancy Prediction).
- **Backend Throughput:** 241 Java 21 classes compiled with zero errors; sub-10ms query execution across 6 Flyway migrations.
- **Frontend Bundle Optimization:** Rollup code-splitting reduced main JavaScript bundle from **1,840 kB to 376 kB (~80% reduction)**.
- **Automated Test Suite:** 8/8 Python tests passed; 25/25 Frontend Vitest unit tests passed across 5 test suites.

#### System Boundaries & Limitations
- **Academic Simulation:** Virtual IoT sensors and synthetic AI benchmarks; no physical hardware connected.
- **Crowd Dynamics:** Graph-level shortest clear path; does not model micro-agent crowd panic or doorway crushing.
- **Life-Safety Disclaimer:** Research prototype; not certified under NFPA 72 or UL 864 standards.

#### Future Roadmap
- BACnet / Modbus IP industrial building automation integration.
- Direct BIM / IFC CAD blueprint ingestion.
- Edge TPU / NPU quantization and deployment on embedded gateway nodes.
