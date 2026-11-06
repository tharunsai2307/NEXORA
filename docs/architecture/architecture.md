# System Architecture & Service Communication

## 1. High-Level Architectural Overview

The **Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System** is designed as a modular monorepo consisting of three decoupled, independently deployable services and an isolated persistent database.

```mermaid
flowchart TD
    subgraph ClientLayer ["Client Layer"]
        Frontend["React + TypeScript Frontend\n(Port 5173 / Nginx 80)\nTailwind CSS · Three.js · React Router"]
    end

    subgraph CoreOrchestrationLayer ["Core Orchestration Layer"]
        Backend["Java 21 Spring Boot Backend\n(Port 8080)\nREST APIs · Data JPA · Security · Actuator"]
    end

    subgraph EdgeIntelligenceLayer ["Edge Intelligence Layer"]
        AiEngine["Python FastAPI Edge-AI Service\n(Port 8000)\nNumPy · Pandas · Scikit-learn"]
    end

    subgraph PersistenceLayer ["Persistence Layer"]
        Database[("PostgreSQL 16 Database\n(Port 5432)\nFlyway Managed Schema")]
    end

    Frontend -->|"HTTP REST API (/api/v1/*)\nJSON Payloads"| Backend
    Backend -->|"Internal HTTP Bridge\nPOST /api/v1/inference/test\nGET /health"| AiEngine
    Backend -->|"JDBC / Connection Pool (HikariCP)\nFlyway Migrations"| Database
```

## 2. Strict Communication Rules Enforced
1. **Frontend Isolation**: The React frontend communicates strictly with the Java Spring Boot Backend over standard REST APIs (`/api/v1/*`). It never connects directly to the Python AI service or PostgreSQL.
2. **AI Service Isolation**: The Python AI Service is a pure stateless microservice. It does **NOT** access the PostgreSQL database. All state, configurations, and raw telemetry are managed and dispatched by the Java backend.
3. **Database Single Custodian**: PostgreSQL is exclusively accessed and mutated by the Java backend via Spring Data JPA and Flyway migrations.

## 3. Communication Sequence Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as Facilities Engineer
    participant Frontend as React Frontend (Vite)
    participant Backend as Spring Boot Core (Java 21)
    participant AI as Edge-AI Engine (FastAPI)
    participant DB as PostgreSQL 16 (Flyway)

    %% System Probe Flow
    User->>Frontend: Loads Dashboard Console
    Frontend->>Backend: GET /api/v1/system/info
    activate Backend
    Backend->>DB: DataSource.getConnection().isValid(2)
    activate DB
    DB-->>Backend: Connection Valid (UP)
    deactivate DB
    Backend->>AI: GET /health
    activate AI
    AI-->>Backend: 200 OK {"status": "UP", "service": "smart-building-ai"}
    deactivate AI
    Backend-->>Frontend: 200 OK (Consolidated System Info)
    deactivate Backend
    Frontend-->>User: Displays Subsystem Heartbeat (All Online)

    %% Future Inference Bridge Verification Flow
    Note over Backend,AI: Telemetry Ingestion & Inference Bridge (Phase 3+)
    Backend->>AI: POST /api/v1/inference/test (Sensor Payload)
    activate AI
    AI-->>Backend: 200 OK {"status": "INFERENCE_ECHO_VERIFIED", ...}
    deactivate AI
```

## 4. Subsystem Topology & Port Allocations
| Service | Technology | Internal Port | External Port | Role |
|---|---|---|---|---|
| **frontend** | React 18, TypeScript, Tailwind, Three.js | 80 / 5173 | 5173 | User visualization, 3D Digital Twin, Control Center |
| **backend** | Java 21, Spring Boot 3.3.5 | 8080 | 8080 | Orchestrator, Auth, Validation, Database custodian |
| **ai-service**| Python 3.11+, FastAPI, Uvicorn | 8000 | 8000 | Anomaly detection, Occupancy predictions |
| **postgres**  | PostgreSQL 16 Alpine | 5432 | 5432 | Relational system of record, migration store |
