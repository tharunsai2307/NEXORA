# NEXORA — Diagnostic & Troubleshooting Runbook

**Product:** NEXORA — Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Audience:** DevOps Engineers, System Administrators, Evaluators

---

## 1. Quick Diagnostic Checklist

When investigating an issue, verify the health status of all four tiers in sequence:

1. **Verify Docker Containers:**
   ```bash
   docker compose ps
   ```
   *Expected:* All containers (`smart_building_postgres`, `smart_building_ai`, `smart_building_backend`, `smart_building_frontend`) should report `Up (healthy)`.

2. **Inspect Container Logs:**
   ```bash
   # Backend Logs
   docker compose logs -f backend

   # Edge AI Service Logs
   docker compose logs -f ai-service

   # Nginx Reverse Proxy Logs
   docker compose logs -f frontend

   # Database Logs
   docker compose logs -f postgres
   ```

3. **Verify Health Endpoints via cURL:**
   ```bash
   # Edge AI Health
   curl -i http://localhost/health (Internal: curl -i http://localhost:8000/health)

   # Backend Health
   curl -i http://localhost/api/v1/health

   # Backend Detailed Actuator Health
   curl -i http://localhost/actuator/health
   ```

---

## 2. Common Operational Issues & Resolutions

### Issue 1: Port Conflict on Host Port 80 or 8080
- **Symptom:** `Error starting userland proxy: listen tcp4 0.0.0.0:80: bind: address already in use`
- **Cause:** Another web server (e.g. Apache, IIS, another Nginx) or dev process is bound to port 80.
- **Resolution:** Modify `FRONTEND_PORT` in your `.env` file:
  ```ini
  FRONTEND_PORT=8085
  ```
  Then restart with `docker compose up -d`. Access the UI at `http://localhost:8085`.

### Issue 2: Backend Fails to Start with `Connection refused to postgres:5432`
- **Symptom:** Backend container exits with `HikariPool-1 - Exception during pool initialization`.
- **Cause:** PostgreSQL container is still initializing `uuid-ossp` or waiting on volume mount.
- **Resolution:**
  - Verify that `backend` has `depends_on: postgres: condition: service_healthy` in `docker-compose.yml`.
  - Check postgres health manually:
    ```bash
    docker compose exec postgres pg_isready -U smart_admin -d smart_building_db
    ```

### Issue 3: 3D Digital Twin Shows Black Screen or WebGL Context Warning
- **Symptom:** Browser console reports `THREE.WebGLRenderer: Context Lost` or renders blank canvas.
- **Cause:** Hardware acceleration is disabled in the client browser, or GPU memory is saturated.
- **Resolution:**
  - In Google Chrome, navigate to `chrome://settings/system` and ensure **"Use graphics acceleration when available"** is toggled **ON**.
  - Refresh the page to allow React Three Fiber to re-initialize the WebGL canvas.

### Issue 4: WebSocket Real-Time Telemetry Disconnected (`WS POLLING` instead of `WS LIVE`)
- **Symptom:** Indicator in 3D Digital Twin or Emergency console displays `WS POLLING` instead of `WS LIVE`.
- **Cause:** Client network proxy or browser extension blocks raw WebSocket upgrades, or Nginx failed to proxy `/ws-raw`.
- **Resolution:**
  - Confirm that `frontend/nginx.conf` contains the `location /ws-raw` block with `Upgrade` and `Connection "upgrade"` headers.
  - The frontend automatically falls back to periodic HTTP polling if WebSockets are unavailable, ensuring continuous data availability.

### Issue 5: Flyway Migration Checksum Mismatch
- **Symptom:** `org.flywaydb.core.api.exception.FlywayValidateException: Validate failed: Checksum mismatch`.
- **Cause:** A migration file (`V1` to `V6`) was modified after having already run on an existing database volume.
- **Resolution:** If running in a development or testing environment:
  ```bash
  docker compose down -v
  docker compose up -d --build
  ```
  *(Caution: `-v` deletes the existing volume and re-creates clean tables from migration scripts).*

### Issue 6: Scikit-Learn Multiprocessing Hang on Windows
- **Symptom:** Python test or worker hangs indefinitely during `predict()` or `score_samples()`.
- **Cause:** `n_jobs=-1` attempts to spawn process pools on Windows IOCP loops without fork support.
- **Resolution:** This was resolved in Phase 8 by setting `n_jobs=1` across all estimators in `anomaly_detector.py` and `occupancy_predictor.py`. Ensure you are running the latest codebase.

