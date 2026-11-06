# Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System

> **Phase 1 Master Architectural Foundation**  
> An enterprise-grade, microservice-based smart building simulation, predictive edge-AI, and interactive 3D digital twin platform.

---

## 1. Project Overview

The **Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System** is an end-to-end software platform designed to simulate modern commercial smart buildings using virtual IoT sensors, process sensor streams with an enterprise Java backend, execute localized edge AI inference (predictive occupancy modeling and environmental anomaly detection) via a dedicated Python AI engine, persist historical data in PostgreSQL, and render real-time spatial telemetry through an interactive 3D WebGL digital twin.

This implementation is software-only; no physical IoT hardware is required.

---

## 2. System Architecture

The project is structured as a **modular monorepo** containing three decoupled, independently runnable services and an isolated relational database:

```
┌────────────────────────────────┐
│   React Frontend (Vite + TS)   │  Port 5173 / Nginx 80
└───────────────┬────────────────┘
                │  HTTP REST (/api/v1/*)
                ▼
┌────────────────────────────────┐
│  Java Spring Boot Backend      │  Port 8080
└───────┬────────────────┬───────┘
        │                │
        │ Internal HTTP  │ JPA / JDBC
        ▼                ▼
┌──────────────┐ ┌──────────────┐
│  Python AI   │ │  PostgreSQL  │
│  (FastAPI)   │ │  16 Database │
│  Port 8000   │ │  Port 5432   │
└──────────────┘ └──────────────┘
```

### Architectural Guardrails
1. **Frontend Isolation**: The React client communicates exclusively with the Java backend via REST APIs. The frontend never accesses PostgreSQL or the Python AI service directly.
2. **AI Engine Isolation**: The Python FastAPI service is a stateless intelligence tier. It never connects directly to PostgreSQL. Inference requests and telemetry are dispatched exclusively by the Java backend.
3. **Database Single Custodian**: PostgreSQL is exclusively managed by the Java backend using Spring Data JPA and automated Flyway schema migrations.

---

## 3. Technology Stack

| Tier | Technologies |
|---|---|
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Three.js, React Three Fiber, React Three Drei, Lucide React, Axios, Vitest |
| **Backend Core** | Java 21 LTS, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Spring Security, Spring Actuator, Spring WebSocket, Flyway, PostgreSQL JDBC, JUnit 5, Mockito |
| **Edge-AI Engine** | Python 3.11+, FastAPI, Uvicorn, Pydantic v2, NumPy, Pandas, Scikit-learn, Pytest, HTTPX |
| **Database** | PostgreSQL 16 Alpine, Flyway Migration Engine, HikariCP Connection Pool |
| **DevOps & Containers**| Docker, Docker Compose, Multi-stage Dockerfiles, Git, PowerShell scripts |

---

## 4. Repository Structure

```
smart-building-digital-twin/
│
├── frontend/                     # React + TypeScript Vite frontend
│   ├── src/
│   │   ├── components/           # Common UI, ServiceStatusBadge, SystemStatusBar, MetricCard
│   │   ├── pages/                # 9 structural routes (Dashboard, Buildings, Sensors, etc.)
│   │   ├── layouts/              # AppLayout, Sidebar, Header
│   │   ├── services/             # Axios API client (apiClient, systemService)
│   │   ├── hooks/                # useSystemStatus polling & refresh hook
│   │   ├── types/                # TypeScript interface definitions
│   │   ├── utils/                # Formatters and helpers
│   │   ├── test/                 # Vitest component & routing test suites
│   │   ├── App.tsx               # Route definitions & router setup
│   │   ├── main.tsx              # DOM root mount
│   │   └── index.css             # Tailwind base & custom scrollbars
│   ├── public/                   # SVG icons and static assets
│   ├── Dockerfile                # Multi-stage production Nginx container
│   ├── nginx.conf                # Nginx reverse proxy configuration
│   ├── package.json              # Frontend dependencies
│   ├── vite.config.ts            # Vite & Vitest configuration
│   └── README.md
│
├── backend/                      # Java 21 Spring Boot core orchestrator
│   ├── src/main/java/com/smartbuilding/
│   │   ├── config/               # CorsConfig, SecurityConfig, AiClientConfig
│   │   ├── common/               # ApiResponse, ErrorResponse, GlobalExceptionHandler
│   │   ├── health/               # HealthController, SystemController, HealthService
│   │   ├── ai/                   # AiServiceClient, REST bridge DTOs
│   │   ├── building/             # Topology package (Phase 2)
│   │   ├── sensor/               # Virtual IoT sensor package (Phase 3)
│   │   ├── simulation/           # Sensor simulation engine (Phase 3)
│   │   ├── occupancy/            # Occupancy analytics package (Phase 3/4)
│   │   ├── safety/               # Environmental safety monitoring (Phase 4)
│   │   ├── digitaltwin/          # 3D spatial state aggregator & WebSocket (Phase 5)
│   │   ├── emergency/            # Evacuation routing & emergency alerts (Phase 6)
│   │   ├── analytics/            # Long-term reporting package (Phase 7)
│   │   └── SmartBuildingApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml       # Production/development configuration
│   │   └── db/migration/         # Flyway SQL migrations (V1__init_schema_foundation.sql)
│   ├── src/test/                 # JUnit 5 & MockMvc automated test suites
│   ├── Dockerfile                # Multi-stage Alpine container
│   ├── pom.xml                   # Maven build descriptor
│   └── README.md
│
├── ai-engine/                    # Python FastAPI Edge-AI intelligence microservice
│   ├── app/
│   │   ├── api/                  # Routes (/health, /info, /api/v1/inference/test)
│   │   ├── schemas/              # Pydantic request & response validation schemas
│   │   ├── services/             # InferenceService & business logic
│   │   ├── models/               # ModelRegistry architecture placeholder
│   │   ├── utils/                # Settings & structured logging
│   │   └── main.py               # FastAPI application entrypoint
│   ├── tests/                    # Pytest test suite (health, info, inference schemas)
│   ├── Dockerfile                # Python 3.11-slim container
│   ├── requirements.txt          # Pinned Python package dependencies
│   └── README.md
│
├── database/                     # PostgreSQL configurations
│   ├── init/                     # Initial container startup scripts (01-init.sql)
│   └── README.md
│
├── docs/                         # Technical documentation
│   ├── architecture/             # architecture.md with Mermaid diagrams
│   ├── uml/                      # UML specifications placeholder
│   ├── srs/                      # Software Requirements Specification
│   └── testing/                  # Test execution logs and coverage details
│
├── scripts/                      # Developer automation scripts
│   ├── run-tests.ps1             # Multi-tier automated test suite runner
│   └── check-health.ps1          # Live HTTP health probe utility
│
├── .env.example                  # Environment variable blueprint
├── .gitignore                    # Comprehensive multi-tier gitignore
├── docker-compose.yml            # Multi-service container orchestration
└── README.md                     # Root project documentation
```

---

## 5. Ports and Service URLs

| Service | Host / Internal Port | Base URL | Swagger / Web Interface |
|---|---|---|---|
| **Frontend** | `http://localhost:5173` | `http://localhost:5173` | Control Center Dashboard |
| **Backend Core** | `http://localhost:8080` | `http://localhost:8080/api/v1` | Actuator: `http://localhost:8080/actuator` |
| **AI Engine** | `http://localhost:8000` | `http://localhost:8000` | Swagger UI: `http://localhost:8000/docs` |
| **PostgreSQL** | `localhost:5432` | `jdbc:postgresql://localhost:5432/smart_building_db` | Direct psql / JDBC |

---

## 6. API Endpoint Reference

### Java Backend Endpoints (`http://localhost:8080/api/v1`)
- `GET /health`  
  Returns `{ "status": "UP", "service": "smart-building-backend" }`
- `GET /health/ai`  
  Checks reachability of the Python AI service. Returns `200 OK` or `503 Service Unavailable`.
- `GET /health/database`  
  Verifies PostgreSQL connection pool validity. Returns `200 OK` or `503 Service Unavailable`.
- `GET /system/info`  
  Returns consolidated platform metadata: application name, version, backend status, database status, AI engine status, and runtime environment.

### Python AI Service Endpoints (`http://localhost:8000`)
- `GET /health`  
  Returns `{ "status": "UP", "service": "smart-building-ai" }`
- `GET /info`  
  Returns service name, version, Python version, installed ML library versions (NumPy, Pandas, Scikit-learn), and model initialization status.
- `POST /api/v1/inference/test`  
  Validates request schema with Pydantic and returns structured echo response confirming Java-to-Python communication.
- `GET /docs`  
  Interactive OpenAPI / Swagger UI documentation.

---

## 7. Local Development Setup

### Prerequisites
- **Node.js**: v20+ and npm
- **Java**: JDK 21 LTS
- **Python**: 3.10+ and pip
- **PostgreSQL**: 16+ (or run via Docker Compose)

### 1. Configure Environment
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

### 2. Start PostgreSQL
```bash
docker compose up -d postgres
```

### 3. Start Python AI Service
```bash
cd ai-engine
python -m venv venv
.\venv\Scripts\activate   # Windows
# source venv/bin/activate # Linux/macOS
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. Start Java Spring Boot Backend
```bash
cd backend
./mvnw spring-boot:run     # Linux/macOS
# .\mvnw.cmd spring-boot:run # Windows
```

### 5. Start React Frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

---

## 8. Docker Compose Deployment

Run all four services with a single command:
```bash
docker compose up -d --build
```

To stop all services:
```bash
docker compose down
```

To inspect container logs:
```bash
docker compose logs -f
```

---

## 9. Automated Testing Execution

All three service tiers contain standalone, fully automated test suites:

### Run All Tests via PowerShell Script
```powershell
.\scripts\run-tests.ps1
```

### Manual Individual Test Commands
- **Python AI Engine**:
  ```bash
  cd ai-engine
  python -m pytest tests/ -v
  ```
- **Java Spring Boot Backend**:
  ```bash
  cd backend
  ./mvnw test
  ```
- **React Frontend**:
  ```bash
  cd frontend
  npm test
  ```

---

## 10. Troubleshooting Guide

| Issue | Likely Cause | Resolution |
|---|---|---|
| **AI Engine status shows Disconnected in UI** | Python AI service is not running on port 8000 | Verify AI service is active via `curl http://localhost:8000/health` or `uvicorn app.main:app --port 8000`. |
| **Database status shows Disconnected in UI** | PostgreSQL container is stopped or port 5432 is occupied | Run `docker compose up -d postgres` and check credentials in `.env`. |
| **CORS errors in browser console** | Backend CORS policy is rejecting origin | Ensure `http://localhost:5173` is included in `cors.allowed-origins` inside `application.yml`. |
| **Flyway migration fails on startup** | Database credentials or existing incompatible schema | Inspect logs; verify database user has `CREATE TABLE` permissions on `smart_building_db`. |
| **Vite port conflict** | Port 5173 is already in use by another Vite process | Terminate the existing node process or pass `--port 5174` in `npm run dev`. |
