# NEXORA — Phase 3 Completion Report
## IoT Sensor Simulation & Real-Time Monitoring Subsystem

**Product Name:** NEXORA  
**Tagline:** Intelligence Beyond Infrastructure  
**Project:** Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Milestone:** Phase 3 of 7 (42% Completed)  
**Date:** March 2026  

---

## 1. Executive Summary

Phase 3 transitions NEXORA into an active, data-generating smart building digital twin platform by implementing a complete **Virtual IoT Sensor Simulation Engine** and **Real-Time Environmental Monitoring Subsystem**.

All virtual IoT devices are backed by persistent PostgreSQL schemas, realistic mathematical drift generation, granular simulation session management, multi-channel WebSocket streaming, manual reading injection capabilities, and an industrial-grade frontend monitoring center.

---

## 2. Key Modules Delivered

### 2.1 Virtual IoT Sensor Fleet & Environmental Modeling
Supports 5 distinct physical and ambient sensor modalities:
1. **Temperature (`TEMPERATURE`):**
   - Unit: Celsius (°C).
   - Baseline: 20°C–26°C with smooth Brownian/mean-reverting fluctuations.
   - Anomaly Mode: Thermal spikes reaching 38°C–70°C.
2. **Relative Humidity (`HUMIDITY`):**
   - Unit: Percent (%).
   - Baseline: 35%–60% with physical correlation to temperature.
   - Anomaly Mode: Arid desiccations (<15%) or saturation condensation (>85%).
3. **Smoke Concentration (`SMOKE`):**
   - Unit: PPM (Parts Per Million).
   - Baseline: Nominal clean air (0.0–2.0 ppm).
   - Anomaly Mode: Fire/combustion ramp up to 100–300+ ppm. Clearly labeled as simulated.
4. **Air Quality Index (`AIR_QUALITY`):**
   - Unit: AQI (0–500 scale).
   - Baseline: Good / Moderate (20–60 AQI).
   - Anomaly Mode: Severe particulate and VOC pollution (200–450 AQI). Clearly labeled as simulated.
5. **Dynamic Occupancy (`OCCUPANCY`):**
   - Unit: People (integer count).
   - Bounds: Strictly non-negative ($0 \le currentOccupancy \le maximumCapacity$).
   - Synchronized: Generates gradual entry/exit patterns tailored by room type (`CLASSROOM`, `LABORATORY`, `OFFICE`, `CONFERENCE`, etc.) and automatically updates `rooms.current_occupancy` in the database.

### 2.2 Simulation Engine & Scenario Automation
- **Multi-threaded Background Scheduler:** Built with Java `ScheduledExecutorService` with non-blocking ticks and safe concurrent execution.
- **Session Lifecycle Control:** Complete state machine (`STOPPED` $\rightarrow$ `RUNNING` $\rightleftharpoons$ `PAUSED` $\rightarrow$ `STOPPED`) with adjustable tick frequency (1s to 60s).
- **6 Built-in Simulation Scenarios:**
  - `NORMAL`: Ambient mean-reverting stochastic fluctuations.
  - `TEMPERATURE_ANOMALY`: Rapid heating event simulating HVAC failure or localized heat source.
  - `SMOKE_EVENT`: Exponential smoke build-up simulating fire outbreak.
  - `POOR_AIR_QUALITY`: Gradual air degradation simulating ventilation failure.
  - `HIGH_OCCUPANCY`: Surging occupant influx testing capacity limits.
  - `CUSTOM`: Configurable parameter-driven scenario with custom target rooms.
- **Batch Data Ingestion:** Readings generated per tick are ingested via batched JPA/JDBC writes for high throughput.

### 2.3 Manual Sensor Reading Injection
- Dedicated operator override API (`POST /api/v1/sensors/{id}/readings/manual`) and interactive UI modal.
- Supports testing edge cases, emergency scenarios, and calibrating downstream AI pipelines.
- Differentiates source metadata (`SIMULATED` vs `MANUAL`).

### 2.4 Real-Time WebSocket Streaming
- Spring WebSocket configured with STOMP message broker (`/ws` with SockJS and `/ws-raw` native).
- Permitted under stateless Spring Security filter chain.
- Broadcast Topics:
  - `/topic/buildings/{id}/sensors`: Fleet-wide readings for a facility.
  - `/topic/buildings/{id}/occupancy`: Instant room occupancy synchronization.
  - `/topic/simulations/{id}`: Simulation session state & tick counters.
  - `/topic/sensors/{id}`: Granular per-sensor real-time telemetry.

### 2.5 Industrial Frontend Control Center
- **Fleet Overview Tab:** High-level sensor KPI counters (Total, Active, Warning, Error), sensor distribution by modality, building fleet metrics, and fast-action simulation launcher.
- **Sensor Topology Explorer Tab:** Interactive hierarchical navigation (Building $\rightarrow$ Floor $\rightarrow$ Room), one-click 5-sensor room auto-provisioning, sensor search & filtering, live status badges, and quick injection triggers.
- **Simulation Control Center Tab:** Live simulation session cards, real-time tick counters, session start/pause/resume/stop controls, and 1-click preset scenario launcher cards.
- **Interactive Sensor Details Modal:** Real-time stats display, historical reading logs, and an SVG sparkline trend chart.
- **Manual Reading Injection Modal:** Direct input with value validation and instant live broadcast update.

---

## 3. Database Schema Updates (Migration V3)

Added 4 relational tables in `V3__sensor_simulation.sql`:
1. `sensors`: Stores sensor configurations, hardware IDs, units, thresholds, and operational status.
2. `sensor_readings`: Time-series sensor measurements with `value`, `timestamp`, `is_anomaly`, and `source` (`SIMULATED` | `MANUAL`). Optimized with compound index `(sensor_id, timestamp DESC)`.
3. `simulation_sessions`: Persistent session state, tick intervals, active scenarios, and cumulative tick counts.
4. `simulation_scenarios`: Scenario definitions, target rooms, severity multipliers, and execution history.

Pre-seeded Data:
- **35 Virtual Sensors** pre-provisioned across all 7 seed rooms in Building 1 (`rm-101`, `rm-102`, `rm-103`, `rm-201`, `rm-202`, `rm-301`, `rm-302`).
- 35 Initial baseline readings for immediate operational visibility.

---

## 4. Verification & Status Metrics

| Metric | Recorded Value |
|---|---|
| **Total Virtual Sensors Pre-Seeded** | 35 sensors across 7 rooms |
| **Sensor Modalities Implemented** | 5 (Temperature, Humidity, Smoke, Air Quality, Occupancy) |
| **Simulation Scenarios Supported** | 6 (Normal, Temp Anomaly, Smoke, Poor AQI, High Occupancy, Custom) |
| **Simulation Engine States** | RUNNING, PAUSED, STOPPED |
| **Tick Interval Range** | 1 to 60 seconds (Default: 3s) |
| **Room Occupancy Synchronization** | Verified (Clamped $0 \le N \le MaxCapacity$) |
| **WebSocket Broker Endpoints** | `/ws`, `/ws-raw` with STOMP `/topic/**` |
| **Backend Java Code Compilation** | 100% BUILD SUCCESS (122 source files) |
| **Frontend Production Build** | 100% SUCCESS (`tsc -b && vite build` in 12.6s) |
| **Phase 4 Isolation** | Strictly enforced (No AI models or inference executed) |

---

## 5. Next Steps: Phase 4 Readiness

Phase 3 is complete and ready. All sensor readings and historical data formats are structured and prepared for direct ingestion into Phase 4:
- Isolation Forest anomaly detection models.
- Random Forest occupancy forecasting models.
- Python FastAPI inference pipeline integration.

*Awaiting explicit user authorization before initiating Phase 4.*
