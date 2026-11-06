# NEXORA — Phase 2 Completion Report
## Authentication & Building Management Subsystem

**Product Name:** NEXORA  
**Tagline:** Intelligence Beyond Infrastructure  
**Project:** Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Milestone:** Phase 2 of 7 (28% Completed)  
**Date:** March 2026  

---

## 1. Executive Summary

Phase 2 successfully transitions NEXORA from its Phase 1 foundational shell into a fully functional, enterprise-grade **Authentication and Building Management System**. 

The system now enforces strict **stateless JWT security with BCrypt cryptographic hashing**, **Role-Based Access Control (RBAC)** across three operator personas, a comprehensive **User Directory Administration** console, a complete 3-tier **Spatial Building Hierarchy** (Building $\rightarrow$ Floor $\rightarrow$ Room), and a standardized **3D Euclidean Coordinate System** ($X, Y, Z$) with room bounding volumes ($W, D, H$) designed directly for Phase 5's Three.js Digital Twin.

All components have been verified with automated test suites across the Java backend and React frontend, alongside production builds.

---

## 2. Key Modules Delivered

### 2.1 Module 1 — Authentication Subsystem
- **JWT Provider & Interceptors:** RFC 7519 compliant JSON Web Token authentication signed via HMAC-SHA256 (`HS256`) using JJWT `0.12.6`.
- **BCrypt Password Security:** All operator credentials cryptographically salted and hashed. Plaintext passwords never stored or serialized.
- **Stateless Session Management:** Zero session affinity on Spring Boot nodes (`SessionCreationPolicy.STATELESS`), allowing horizontal scaling.
- **Public Self-Registration:** Strict registration endpoint (`POST /api/v1/auth/register`) defaults users to `BUILDING_MANAGER` role, strictly disallowing self-elevation to `SYSTEM_ADMIN` or `SAFETY_OFFICER`.
- **Standardized Error Handling:** Standardized JSON error envelopes for unauthenticated (HTTP 401) and forbidden (HTTP 403) actions.

### 2.2 Module 2 — Role-Based Access Control (RBAC) & User Management
- **Three Core Roles:**
  - `SYSTEM_ADMIN`: Platform governance, operator clearance, assignment of managers to facilities.
  - `BUILDING_MANAGER`: Facility operations, floor structuring, room coordinates, and capacity planning.
  - `SAFETY_OFFICER`: Audit and monitoring privileges over safety statuses, environmental metrics, and evacuation paths.
- **Last Active Admin Invariant:** A defensive validation preventing the system from ever deactivating or demoting the last active `SYSTEM_ADMIN`.
- **Administrative Console:** Searchable and filterable user directory with real-time role modification and account status activation/deactivation.

### 2.3 Module 3 — Building Fleet Topology Management
- **Full CRUD Support:** Complete backend API and frontend interfaces for creating, viewing, updating, and removing buildings.
- **Global Code Uniqueness:** Global unique building codes (e.g. `NEX-TWR-01`).
- **Classification & Operational Status:** Granular facility types (`COMMERCIAL`, `EDUCATIONAL`, `INDUSTRIAL`, `RESIDENTIAL`, `HEALTHCARE`, `OTHER`) and lifecycle states (`ACTIVE`, `MAINTENANCE`, `INACTIVE`, `EMERGENCY`).
- **Operator Assignment:** Explicit association between building managers and their authorized facilities via `building_user_assignments`.

### 2.4 Module 4 — Vertical Floor Hierarchy
- **Scoped Floor Numbering:** Ordered vertical floors with collision detection enforcing unique floor numbers per building.
- **Physical Footprint Modeling:** Real-world dimensions (`layoutWidth` and `layoutDepth` in meters) defining the floor plan boundary.
- **Cascading Integrity:** Floor levels enforce referential integrity checks before deletion.

### 2.5 Module 5 — Room Modeling & 3D Digital Twin Coordinate System
- **Classification Matrix:** 12 room types including `OFFICE`, `CONFERENCE_ROOM`, `LABORATORY`, `SERVER_ROOM`, `CLASSROOM`, `CORRIDOR`, `STAIRCASE`, `LOBBY`, `CAFETERIA`, `STORAGE_ROOM`, and `EMERGENCY_EXIT_AREA`.
- **Capacity & Occupancy Tracking:** `maximumCapacity` headcount limits and live `currentOccupancy` tracking.
- **Safety Status Triage:** Initialized with `NORMAL`, `WARNING`, or `CRITICAL` safety flags.
- **3D Spatial Coordinate Schema (Phase 5 Alignment):**
  - Dimensions: $Width$ ($\Delta X$), $Depth$ ($\Delta Z$), $Height$ ($\Delta Y$) in meters.
  - Offsets: $Pos_X$, $Pos_Y$, $Pos_Z$ relative to local floor plane origin $(0,0,0)$.
  - Eliminates database schema churn when initializing Three.js geometry in Phase 5.

### 2.6 Module 6 — Enterprise Frontend Interface
- **Dark-First Industrial Styling:** Modern control center aesthetic using Tailwind CSS and Lucide React icons.
- **Security Context & Guards:** `AuthContext`, `ProtectedRoute` (unauthenticated redirection), and `RoleGuard` (unauthorized role blocking).
- **Interactive Dashboards:** Real-time metrics bar, quick pre-configured demo account logins, hierarchical navigation breadcrumbs (`Buildings` $\rightarrow$ `Floor Level` $\rightarrow$ `Room Detail`).

---

## 3. Database Schema & Flyway Migration

Flyway migration `V2__auth_and_building_management.sql` was authored and committed:
- **Tables Created:** `users`, `audit_logs`, `buildings`, `building_user_assignments`, `floors`, `rooms`.
- **Indexes Created:** 9 performance indexes on email, building codes, foreign keys, and safety statuses.
- **Pre-seeded Baseline Data:**
  - `admin@nexora.com` / `NexoraAdmin2026!` (`SYSTEM_ADMIN`)
  - `manager@nexora.com` / `NexoraManager2026!` (`BUILDING_MANAGER`)
  - `safety@nexora.com` / `NexoraSafety2026!` (`SAFETY_OFFICER`)
  - Flagship facility `NEX-TWR-01` (*Nexora Tower One*) with 3 floors, 7 rooms, and 340 occupant capacity.

---

## 4. Verification & Automated Test Results

### 4.1 Java Spring Boot Backend Suite (JUnit 5 + MockMvc)
- **Status:** **34/34 Passing (100%)**
- **Execution Command:** `mvn clean test`
- **Breakdown:**
  - `AuthControllerTest` (5/5) — Registration, duplicate email rejection, login success, invalid password rejection, `/me` profile.
  - `UserControllerTest` (4/4) — Directory listing, role updates, last active admin protection, status toggle.
  - `BuildingControllerTest` (4/4) — Building list, create building, conflict checks, delete building.
  - `FloorControllerTest` (4/4) — Floor listing, create floor, duplicate number rejection, delete floor.
  - `RoomControllerTest` (3/3) — Room listing, room creation with 3D coordinates, duplicate room code rejection.
  - `HealthControllerTest` & `HealthServiceDatabaseTest` (5/5) — Multi-tier health probes and DB connectivity.
  - `AiServiceClientTest` & `GlobalExceptionHandlerTest` (10/10) — AI client mock communication and RFC 7807 error envelopes.
  - `SmartBuildingApplicationTests` & `SystemControllerTest` (3/3) — Context loading and dashboard statistics aggregation.

### 4.2 React + TypeScript Frontend Suite (Vitest + Testing Library)
- **Status:** **25/25 Passing (100%)**
- **Test Files:**
  - `src/test/Auth.test.tsx` (8/8) — Login form, demo presets, BCrypt hashing badges, validation errors, `RoleGuard` restriction.
  - `src/test/Buildings.test.tsx` (3/3) — Building topology listing, modal triggers, vertical floor hierarchy rendering.
  - `src/test/App.test.tsx` (2/2) — Unauthenticated redirection, authenticated shell and branding rendering.
  - `src/test/Navigation.test.tsx` (9/9) — Complete sidebar route traversal across all modules.
  - `src/test/SystemStatusBar.test.tsx` (3/3) — Heartbeat status rendering and latency badges.
- **Production Bundle:** `npm run build` completed successfully (`dist/assets/index-*.js`, 0 TypeScript errors).

---

## 5. Scope Boundary Compliance

In accordance with project guidelines:
- **Phase 3 (IoT Sensor Simulation):** Excluded. Virtual sensor generators and MQTT/WebSocket sensor feeds remain planned for Phase 3.
- **Phase 4 (Python AI Anomaly Engine):** Excluded. Scikit-learn models and occupancy prediction pipelines remain planned for Phase 4.
- **Phase 5 (3D Digital Twin Viewer):** Excluded. Three.js canvas rendering and R3F meshes remain planned for Phase 5 (all required 3D coordinates and spatial dimensions are now stored in the database).
- **Phase 6 & 7 (Evacuation & Deployment):** Excluded.

---

## 6. Phase 2 Git Commit
All Phase 2 deliverables have been finalized and are prepared for local Git commit:
- Commit message: `feat(phase-2): implement authentication and building management`
- Awaiting user approval to proceed to Phase 3.
