# NEXORA — Installation, Deployment & Operations Guide

**Product:** NEXORA — Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Version:** 2.0 (Phase 9 - Final Deployment)  
**Deployment Modalities:** Docker Compose (Primary Production) & Standalone Local Host (Development)

---

## 1. Prerequisites & System Requirements

### Production Docker Deployment (Recommended)
- **OS:** Linux (Ubuntu 22.04+ / Debian 12+), macOS (Apple Silicon or Intel), or Windows 10/11 with WSL2.
- **Container Engine:** Docker Engine 24.0+ & Docker Compose v2.20+.
- **Hardware Resources:**
  - **RAM:** Minimum 4 GB available (8 GB recommended).
  - **CPU:** 2 cores minimum (4 cores recommended).
  - **Disk Space:** 5 GB free disk space for Docker image build layers and PostgreSQL volume.

### Standalone Local Development
- **Java:** OpenJDK 21 LTS (or compatible JRE).
- **Node.js:** Node 20.x or 22.x LTS with npm 10+.
- **Python:** Python 3.11+ (Python 3.11 / 3.12 / 3.13 / 3.14).
- **Database:** PostgreSQL 15+ or 16+ running locally on port 5432 (or use H2 in-memory via `application-standalone.yml`).

---

## 2. Docker Compose Quickstart Deployment

Follow these three steps to deploy the complete, production-hardened NEXORA stack:

### Step 1: Clone Repository & Configure Environment
```bash
git clone https://github.com/tharunsai2307/NEXORA.git
cd NEXORA

# Create active environment file from template
cp .env.example .env
```
*(On Windows PowerShell, use `Copy-Item .env.example .env`)*

Review `.env` and customize secrets as required:
```ini
FRONTEND_PORT=80
DB_USER=smart_admin
DB_PASSWORD=your_secure_password_here
JWT_SECRET=your_minimum_32_character_secret_key_2026_hex_or_string!
```

### Step 2: Build & Start Containers
```bash
# Production mode (internal PostgreSQL & AI service; Nginx public on port 80)
docker compose up -d --build
```
*Optional Development Mode (exposing DB 5432, FastAPI 8000, Backend 8080 directly):*
```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

### Step 3: Verify Startup & Container Health
```bash
docker compose ps
```
Expected output:
```
NAME                      STATUS                  PORTS
smart_building_postgres   Up (healthy)            5432/tcp
smart_building_ai         Up (healthy)            8000/tcp
smart_building_backend    Up (healthy)            8080/tcp
smart_building_frontend   Up (healthy)            0.0.0.0:80->80/tcp
```

### Step 4: Access Application
Open your web browser and navigate to:
```
http://localhost (or http://localhost:80)
```
Sign in using any preconfigured role:
- **System Administrator:** `admin@nexora.com` / `Admin@123`
- **Building Manager:** `manager@nexora.com` / `Manager@123`
- **Safety Officer:** `safety@nexora.com` / `Safety@123`
- **Building Occupant:** `occupant@nexora.com` / `Occupant@123`

---

## 3. Standalone Local Development Deployment

If Docker is not installed on your development workstation, you can run NEXORA using either the automated batch launcher or manual CLI commands.

### 3.0 Automated Windows Launcher (Recommended)
Simply double-click [`open.bat`](../open.bat) or [`start.bat`](../start.bat) in the project root:
```cmd
open.bat
```
This script handles JDK 21+ `JAVA_HOME` resolution, OpenBLAS thread safety, spawns the 3 microservices in dedicated terminal windows, seeds in-memory data, and automatically launches `http://localhost:5173` in your default browser. To stop all services, run [`stop.bat`](../stop.bat).

---

### Manual Standalone Service Startup

### 3.1 Start Python AI Service
```bash
cd ai-engine
python -m venv venv

# Activate virtual environment
# On Linux/macOS: source venv/bin/activate
# On Windows: .\venv\Scripts\Activate.ps1

pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
*Verification:* Navigate to `http://localhost:8000/docs` to inspect interactive Swagger documentation.

### 3.2 Start Java Spring Boot Backend
```bash
cd backend

# Option A: With Local PostgreSQL on port 5432
./mvnw spring-boot:run

# Option B: Fully Standalone using embedded in-memory H2 database
./mvnw spring-boot:run -Dspring-boot.run.profiles=standalone
```
*(On Windows PowerShell, use `.\mvnw.cmd` and ensure `$env:JAVA_HOME` points to your JDK 21 installation)*  
*Verification:* Navigate to `http://localhost:8080/api/v1/health` (returns `{"status":"UP"}`).

### 3.3 Start React Frontend
```bash
cd frontend
npm install
npm run dev
```
*Verification:* Navigate to `http://localhost:5173` to access the development dashboard.

---

## 4. Environment Variables Reference

| Variable Name | Required | Default in Docker | Description |
| :--- | :--- | :--- | :--- |
| `APP_ENV` | No | `production` | Deployment runtime flag (`development`, `production`). |
| `SPRING_PROFILES_ACTIVE`| No | `prod` | Active Spring profile (`dev`, `prod`, `standalone`). |
| `FRONTEND_PORT` | No | `80` | Host port mapped to Nginx reverse proxy. |
| `DB_HOST` | Yes | `postgres` | Hostname of PostgreSQL database. |
| `DB_PORT` | No | `5432` | Port of PostgreSQL database. |
| `DB_NAME` | Yes | `smart_building_db` | Relational database schema name. |
| `DB_USER` | Yes | `smart_admin` | Database username. |
| `DB_PASSWORD` | Yes | `smart_building_secure_pass_2026` | Database password. |
| `AI_SERVICE_URL` | Yes | `http://ai-service:8000`| URL used by backend to invoke Python AI. |
| `JWT_SECRET` | Yes | *(32+ char default)* | HMAC-SHA256 secret key for signing user sessions. |
| `JWT_EXPIRATION_MS` | No | `86400000` (24h) | JWT validity window in milliseconds. |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost,http://localhost:80` | Comma-separated allowed CORS browser origins. |
| `VITE_API_BASE_URL` | No | `""` (Relative) | Override for frontend REST base URL. |
| `VITE_WS_URL` | No | `""` (Relative) | Override for frontend WebSocket endpoint. |

---

## 5. Maintenance, Upgrades & Backups

### Stopping the Stack
```bash
docker compose down
```

### Resetting & Wiping Data (Fresh Install)
```bash
docker compose down -v
docker compose up -d --build
```

### Backing Up PostgreSQL Database
```bash
docker compose exec postgres pg_dump -U smart_admin smart_building_db > nexora_backup_$(date +%Y%m%d).sql
```

### Restoring Database from Backup
```bash
cat nexora_backup_20261112.sql | docker compose exec -T postgres psql -U smart_admin -d smart_building_db
```

