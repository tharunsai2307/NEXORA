# PostgreSQL Database & Flyway Migrations

Part of the **Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System**.

## Overview
PostgreSQL 16 serves as the persistent system of record. In adherence to architectural constraints:
- **Authoritative Custodian**: Only the Java Spring Boot Backend directly connects to and manages PostgreSQL.
- **Microservice Isolation**: The Python AI Service does **NOT** read or write PostgreSQL directly. It receives payloads strictly via internal HTTP APIs from the Java backend.
- **Migration Engine**: Flyway automates schema evolutions, tracking applied scripts in `flyway_schema_history`.

## Schema Strategy by Phase
- **Phase 1 (Active)**: Minimal schema baseline tracking system metadata (`system_metadata`) to establish migration health and verify JDBC connectivity.
- **Phase 2**: Relational topology (Campus, Building, Floor, Zone, Room) and Security credentials.
- **Phase 3**: Virtual IoT Sensor registry and raw sensor reading timeseries.
- **Phase 4**: Anomaly events, IAQ benchmarks, and safety thresholds.
- **Phase 5**: 3D Digital Twin spatial coordinates, camera viewpoints, and material configurations.
- **Phase 6**: Evacuation graph nodes, hazard zones, and emergency dispatch logs.
- **Phase 7**: Aggregated hourly/daily telemetry metrics and audit history.

## Environment Variables
| Variable | Default Value | Description |
|---|---|---|
| `DB_HOST` | `localhost` (local) / `postgres` (docker) | Database server host |
| `DB_PORT` | `5432` | Standard PostgreSQL port |
| `DB_NAME` | `smart_building_db` | Application database name |
| `DB_USER` | `smart_admin` | Service database user |
| `DB_PASSWORD` | `smart_building_secure_pass_2026` | Database access credential |

## Docker Compose Quickstart
```bash
docker compose up -d postgres
```
Connect via psql:
```bash
psql -h localhost -p 5432 -U smart_admin -d smart_building_db
```
