# Smart Building Core Backend Service

Part of the **Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System**.

## Overview
The Java backend is the central orchestrator and authoritative data custodian of the smart building platform. It handles API requests from the React frontend, manages PostgreSQL persistence and Flyway migrations, and dispatches inference requests to the Python AI engine.

## Technology Stack
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.5
- **Modules**:
  - `spring-boot-starter-web`: High-performance RESTful APIs
  - `spring-boot-starter-data-jpa`: Relational ORM mapping
  - `spring-boot-starter-validation`: Jakarta Bean validation
  - `spring-boot-starter-actuator`: Operational metrics and probes
  - `spring-boot-starter-security`: Stateless security infrastructure (prepared for Phase 2)
  - `spring-boot-starter-websocket`: Real-time duplex channels (prepared for Phase 4+)
  - `postgresql`: Production JDBC driver
  - `flyway-core`: Automated schema migration management
  - `h2`: In-memory isolation database for unit/integration testing
- **Build Tool**: Apache Maven

## Module Organization
```
com.smartbuilding
├── config/         # Security, CORS, RestClient, and infrastructure beans
├── common/         # Global exception handlers, ApiResponse, and domain exceptions
├── health/         # System, database, and AI service health checks
├── building/       # Building topology and spatial management (Phase 2)
├── sensor/         # Virtual IoT sensor registrations (Phase 3)
├── simulation/     # Synthetic sensor simulation engine (Phase 3)
├── occupancy/      # Room occupancy prediction orchestrator (Phase 3/4)
├── ai/             # RestClient client bridging to Python AI engine
├── safety/         # Environmental safety & threshold alerts (Phase 4)
├── digitaltwin/    # 3D spatial state aggregator & WebSocket hub (Phase 5)
├── emergency/      # Evacuation routing and emergency alarms (Phase 6)
└── analytics/      # Historical aggregation & reporting (Phase 7)
```

## REST API Endpoints
| HTTP Method | Path | Description | Expected Status |
|---|---|---|---|
| `GET` | `/api/v1/health` | Backend status verification | `200 OK` |
| `GET` | `/api/v1/health/ai` | Probes Python AI service reachability | `200 OK` or `503 Unavailable` |
| `GET` | `/api/v1/health/database` | Probes PostgreSQL connectivity via connection pool | `200 OK` or `503 Unavailable` |
| `GET` | `/api/v1/system/info` | Comprehensive platform and subsystem status | `200 OK` |
| `GET` | `/actuator/health` | Spring Boot Actuator standard health check | `200 OK` |

## Local Execution

### Prerequisites
- JDK 21 (or compatible OpenJDK runtime)
- Maven 3.9+ (or use bundled wrapper)
- PostgreSQL 16+ running on `localhost:5432`

### Run Tests
```bash
mvn clean test
```

### Run Application
```bash
mvn spring-boot:run
```
Or with custom environment overrides:
```bash
DB_HOST=localhost DB_PORT=5432 DB_NAME=smart_building_db DB_USER=smart_admin DB_PASSWORD=smart_building_secure_pass_2026 AI_SERVICE_URL=http://localhost:8000 mvn spring-boot:run
```
