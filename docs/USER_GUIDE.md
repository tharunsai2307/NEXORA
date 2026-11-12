# NEXORA — User & Administrator Operations Manual

**Product:** NEXORA — Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Audience:** System Administrators, Facility Managers, Safety Officers, and Building Occupants

---

## 1. User Roles & Permission Matrix

NEXORA implements strict Role-Based Access Control (RBAC):

| Feature / Action | `SYSTEM_ADMIN` | `BUILDING_MANAGER` | `SAFETY_OFFICER` | `OCCUPANT` |
| :--- | :---: | :---: | :---: | :---: |
| **View Dashboard & 3D Digital Twin** | Full | Full | Full | Read-Only |
| **User Directory & Role Assignment** | Manage | View Only | No Access | No Access |
| **Building, Floor & Room Topologies**| Create/Edit | Create/Edit | View Only | View Only |
| **Virtual Sensors & Fleet Controls** | Full | Full | View Only | No Access |
| **Simulation Scenarios Injection** | Full | Full | Full | No Access |
| **AI Model Status & Synthetic Retrain**| Retrain | View Only | View Only | No Access |
| **Incident Management (Declare/Update)**| Full | Full | Full | Report Only |
| **Dynamic Evacuation Routing** | Full | Full | Full | View Route |
| **Corridor Blockade Toggling** | Full | Full | Full | No Access |
| **Analytics & Historical Telemetry** | Full | Full | Full | No Access |
| **Post-Mortem Studio & PDF Export** | Full | Full | Full | No Access |

---

## 2. Navigating the Platform Modules

### 2.1 Authentication & Quick-Demo Login
1. Navigate to the login screen.
2. Under **"Select Preconfigured Demo Role"**, click one of the four quick-login buttons:
   - **System Administrator** (`admin@nexora.com`)
   - **Building Manager** (`manager@nexora.com`)
   - **Safety Officer** (`safety@nexora.com`)
   - **Building Occupant** (`occupant@nexora.com`)
3. The platform generates and stores a secure JWT token, loading the customized navigation console.

### 2.2 Executive Dashboard (`/dashboard`)
- **System Heartbeat Bar:** Continuously polls backend, database, and edge-AI health statuses.
- **Top Metric Cards:** High-level counts of buildings, floors, rooms, active sensors, and building occupancy percentage.
- **Environmental Summary:** Real-time pie charts detailing room safety distributions (`NORMAL`, `WARNING`, `CRITICAL`).

### 2.3 3D Building Digital Twin (`/digital-twin`)
- **Interactive Orbit Camera:** Click and drag with left mouse button to rotate; right mouse button to pan; scroll wheel to zoom.
- **Camera Presets:** Use toolbar buttons for instant viewpoints (`TOP`, `FRONT`, `SIDE`, `RESET`).
- **Exploded View Slider:** Drag the **"Exploded Floor Gap"** slider ($0.0\text{m} - 15.0\text{m}$) to separate floors vertically in 3D space.
- **Display Modes:**
  - `RISK`: Rooms color-coded by AI composite safety threat score (Green = Normal, Yellow = Warning, Orange = High Risk, Red = Critical).
  - `OCCUPANCY`: Color-coded by crowd utilization percentage.
  - `TEMPERATURE`: Heatmap showing ambient Celsius readings.
  - `SMOKE` / `AIR_QUALITY`: Particulate visualization.
  - `EMERGENCY`: Highlights active incident origin rooms and renders dynamic 3D egress vectors.
- **Room Inspection Panel:** Click any room in the 3D scene to open its detail sheet with instantaneous telemetry, AI anomaly probabilities, and occupant count.

### 2.4 Virtual IoT Sensors & Fleet Management (`/sensors`)
- **Sensor Fleet Inventory:** Filter by sensor category (`TEMPERATURE`, `HUMIDITY`, `SMOKE`, `AIR_QUALITY`, `OCCUPANCY`).
- **Simulation Control Panel:**
  - Click **"Start Simulation"** to launch the scheduled background generator.
  - Select active scenarios: **"Heatwave / Thermal Spike"**, **"Fire Hazard / Smoke Event"**, **"High Crowd Density"**, or **"Sensor Degradation"**.
  - Monitor readings streaming in real time over STOMP WebSockets.

### 2.5 Edge AI Intelligence (`/safety` & `/occupancy`)
- **Safety Risk Hub (`/safety`):** Inspect Isolation Forest anomaly detections, severity classifications, and contributing feature breakdowns.
- **Occupancy Prediction (`/occupancy`):** View Random Forest 15-minute ahead forecasts, crowd dynamics, and space utilization rates.
- **Model Registry Status:** Inspect active model versions, training sample sizes, and validation precision/recall.

### 2.6 Emergency Operations Center (EOC) (`/emergency`)
- **Active Incident Tracker:** Real-time list of detected, active, and acknowledged incidents.
- **Incident Escalation:** Declare new emergency incidents manually or observe automated detection from smoke/temperature thresholds.
- **Dynamic Evacuation Routing:**
  - Click **"Calculate Evacuation Routes"** to trigger weighted Dijkstra egress calculation.
  - Inspect path node sequences, floor transitions (e.g. *Level 2 $\to$ Level 1 $\to$ Ground Exit*), and distance in meters.
  - Click **"Toggle Block"** on any corridor or stairwell connection to simulate a collapsed path; observe instant path recalculation around the obstacle.

### 2.7 Analytics, Post-Mortem & Reporting Studio (`/analytics`)
- **Historical Telemetry Charts:** Interactive multi-series area and line charts exploring sensor trends over customized time windows.
- **Incident Post-Mortem Studio:**
  - Select any resolved or active incident.
  - Click **"Generate AI Post-Mortem Draft"** to synthesize timeline narratives, sensor min/max/average statistics, and root cause assessments.
  - Edit corrective action items, mark report as **"REVIEWED"** or **"FINALIZED"**.
- **Executive PDF Export:** Click **"Download Official PDF"** to stream a formal, formatted executive incident report.

