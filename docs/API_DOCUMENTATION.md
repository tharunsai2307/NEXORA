# NEXORA — API & Real-Time WebSocket Documentation

**System:** NEXORA — Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Protocol:** HTTP/1.1 REST & STOMP over WebSocket  
**Base URL:** `/api/v1` (Production through Nginx) or `http://localhost:8080/api/v1` (Standalone Backend)  
**WebSocket URL:** `ws://<host>/ws-raw` (or `/ws` for SockJS fallback)

---

## 1. Authentication & Security Headers

All protected endpoints require an `Authorization` header containing a valid JSON Web Token (JWT):
```http
Authorization: Bearer <jwt-token>
```
Public endpoints:
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/health`
- `GET /api/v1/system/info`

---

## 2. Complete REST API Reference

### 2.1 Authentication & User Management
| Method | Endpoint | Access Tier | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Public | Authenticates user; returns JWT token and user profile. |
| `POST` | `/api/v1/auth/register` | Public | Registers a new occupant account. |
| `GET` | `/api/v1/auth/me` | Authenticated | Returns current authenticated user context. |
| `GET` | `/api/v1/users` | Admin / Manager | Returns user directory with roles and account statuses. |
| `PUT` | `/api/v1/users/{id}/role` | Admin | Updates user role (`SYSTEM_ADMIN`, `BUILDING_MANAGER`, etc.). |

### 2.2 Building Topology Management
| Method | Endpoint | Access Tier | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/buildings` | Authenticated | Lists all registered building topologies. |
| `POST` | `/api/v1/buildings` | Admin / Manager | Creates a new building entry. |
| `GET` | `/api/v1/buildings/{id}` | Authenticated | Returns building details including floor and room hierarchy. |
| `GET` | `/api/v1/buildings/{id}/floors` | Authenticated | Returns floors associated with specified building. |
| `POST` | `/api/v1/buildings/{id}/floors` | Admin / Manager | Adds a new floor to a building. |
| `GET` | `/api/v1/floors/{id}/rooms` | Authenticated | Returns rooms associated with specified floor. |
| `POST` | `/api/v1/floors/{id}/rooms` | Admin / Manager | Adds a new room with 3D spatial coordinates. |
| `PUT` | `/api/v1/rooms/{id}/layout` | Admin / Manager | Updates room 3D coordinates `(posX, posY, posZ, width, depth)`. |

### 2.3 Virtual IoT Fleet & Sensor Simulation
| Method | Endpoint | Access Tier | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/sensors` | Authenticated | Lists registered virtual sensors with optional filters. |
| `POST` | `/api/v1/rooms/{roomId}/sensors`| Admin / Manager | Provisions a new sensor for a room. |
| `GET` | `/api/v1/sensors/{id}/readings` | Authenticated | Fetches historical readings for a sensor. |
| `POST` | `/api/v1/simulations/start` | Admin / Manager | Starts a new real-time simulation session. |
| `POST` | `/api/v1/simulations/{id}/pause`| Admin / Manager | Pauses active telemetry worker loops. |
| `POST` | `/api/v1/simulations/{id}/resume`| Admin / Manager| Resumes paused simulation session. |
| `POST` | `/api/v1/simulations/{id}/stop` | Admin / Manager | Terminates simulation session. |
| `POST` | `/api/v1/simulations/{id}/scenarios` | Admin / Manager | Triggers an active scenario (e.g., `FIRE_HAZARD`). |

### 2.4 Edge AI Intelligence Microservice
| Method | Endpoint | Access Tier | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/ai/models` | Authenticated | Returns registered AI model architectures and metadata. |
| `GET` | `/api/v1/ai/models/status` | Authenticated | Returns operational readiness of ML registry. |
| `POST` | `/api/v1/ai/models/retrain` | Admin | Retrains models using calibrated synthetic dataset. |
| `POST` | `/api/v1/inference/anomaly` | Internal / Admin | Isolation Forest multi-modal anomaly scoring. |
| `POST` | `/api/v1/inference/occupancy` | Internal / Admin | Random Forest 15-minute occupancy forecasting. |
| `POST` | `/api/v1/inference/risk` | Internal / Admin | Multi-criteria explainable room risk evaluation. |

### 2.5 3D Digital Twin Spatial Data
| Method | Endpoint | Access Tier | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/digital-twin/buildings/{id}` | Authenticated | Consolidated 3D building payload (floors, rooms, live state). |

### 2.6 Emergency Operations & Evacuation
| Method | Endpoint | Access Tier | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/incidents` | Authenticated | Paginated incident list with status, category, severity filters. |
| `POST` | `/api/v1/incidents` | Safety / Manager | Logs a new emergency incident. |
| `PUT` | `/api/v1/incidents/{id}/status` | Safety / Manager | Transitions incident lifecycle (`ACKNOWLEDGED`, `RESOLVED`, `CLOSED`). |
| `PUT` | `/api/v1/incidents/{id}/assign` | Safety / Manager | Assigns an officer to an active incident. |
| `GET` | `/api/v1/evacuation/status/{buildingId}` | Authenticated | Returns current building emergency & evacuation status. |
| `GET` | `/api/v1/evacuation/graph/{buildingId}` | Authenticated | Returns egress navigation nodes and connections. |
| `POST` | `/api/v1/evacuation/calculate` | Safety / Manager | Computes weighted Dijkstra routes to safe exits. |
| `GET` | `/api/v1/evacuation/routes/{buildingId}` | Authenticated | Returns active 3D polyline waypoints for all rooms. |
| `POST` | `/api/v1/evacuation/connections/{id}/toggle-block` | Safety / Manager | Toggles corridor blockade; triggers immediate dynamic reroute. |

### 2.7 Analytics, Post-Mortem & Reports
| Method | Endpoint | Access Tier | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/analytics/overview/{buildingId}` | Authenticated | High-level metrics (sensors, anomalies, occupancy, incidents). |
| `GET` | `/api/v1/analytics/telemetry/historical` | Authenticated | Time-bucketed historical queries for charting. |
| `POST` | `/api/v1/analytics/post-mortem/generate` | Safety / Manager | Synthesizes an automated incident post-mortem draft. |
| `PUT` | `/api/v1/analytics/post-mortem/{id}` | Safety / Manager | Updates or finalizes post-mortem report. |
| `POST` | `/api/v1/reports/generate` | Manager / Admin | Generates executive report and catalogs entry. |
| `GET` | `/api/v1/reports/incident/{id}/download` | Authenticated | Streams structured formal PDF incident report. |

---

## 3. Real-Time WebSocket / STOMP Topic Catalog

Clients establish a connection to `/ws-raw` using the STOMP 1.2 protocol and subscribe to destination topics:

### 3.1 Sensor & Telemetry Topics
- **/topic/buildings/{buildingId}/sensors**  
  *Payload:* Single `SensorReadingResponse` object emitted on reading persistence.
- **/topic/buildings/{buildingId}/sensors/batch**  
  *Payload:* Array of `SensorReadingResponse` objects emitted at simulation ticks.
- **/topic/buildings/{buildingId}/occupancy**  
  *Payload:* `{"roomId": "...", "currentOccupancy": 14, "maximumCapacity": 30, "timestamp": 1711200000}`.

### 3.2 Edge AI Intelligence Topics
- **/topic/buildings/{buildingId}/ai/anomalies**  
  *Payload:* `{"roomId": "...", "anomalyDetected": true, "severity": "HIGH", "anomalyScore": 0.82}`.
- **/topic/buildings/{buildingId}/ai/predictions**  
  *Payload:* `{"roomId": "...", "predictedOccupancy": 18, "confidenceScore": 0.94}`.
- **/topic/buildings/{buildingId}/ai/risk**  
  *Payload:* `{"roomId": "...", "riskLevel": "CRITICAL", "overallRiskScore": 88.5}`.

### 3.3 Emergency & Evacuation Topics
- **/topic/buildings/{buildingId}/incidents**  
  *Payload:* Real-time `IncidentResponse` on status transition or officer assignment.
- **/topic/buildings/{buildingId}/emergency**  
  *Payload:* `BuildingEmergencyStatusResponse` (active incidents count, clearance status).
- **/topic/buildings/{buildingId}/routes**  
  *Payload:* Array of `EvacuationRouteResponse` containing updated 3D coordinates after Dijkstra recalculation.
- **/topic/buildings/{buildingId}/alerts**  
  *Payload:* High-priority broadcast payload triggering sound/visual cues in the frontend.
