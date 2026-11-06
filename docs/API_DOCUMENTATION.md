# NEXORA — REST API Reference Specification (Phase 2)

## Base URL
```
http://localhost:8080/api/v1
```

All responses conform to the standard RFC-compliant JSON envelope:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-03-30T10:00:00Z"
}
```

---

## 1. Authentication Endpoints

### 1.1 User Self-Registration
- **Endpoint:** `POST /auth/register`
- **Access:** Public
- **Description:** Registers a new operator account. Always assigns `BUILDING_MANAGER` role.
- **Request Body:**
```json
{
  "fullName": "Elena Rostova",
  "email": "elena@nexora.com",
  "password": "SecurePassword2026!",
  "phoneNumber": "+1-555-0182"
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresInSeconds": 86400,
    "user": {
      "id": "usr-001",
      "fullName": "Elena Rostova",
      "email": "elena@nexora.com",
      "role": "BUILDING_MANAGER",
      "status": "ACTIVE",
      "phoneNumber": "+1-555-0182"
    }
  }
}
```

### 1.2 User Login
- **Endpoint:** `POST /auth/login`
- **Access:** Public
- **Description:** Validates credentials and returns signed JWT access token.
- **Request Body:**
```json
{
  "email": "admin@nexora.com",
  "password": "NexoraAdmin2026!"
}
```
- **Response (200 OK):** Returns JWT token and User DTO.

### 1.3 User Logout
- **Endpoint:** `POST /auth/logout`
- **Access:** Authenticated (Bearer Token)
- **Response (200 OK):** Logs audit event; client clears token.

### 1.4 Get Current User Profile
- **Endpoint:** `GET /auth/me`
- **Access:** Authenticated (Bearer Token)
- **Response (200 OK):** Returns authenticated user profile.

---

## 2. User Administration Endpoints (`SYSTEM_ADMIN` Only)

### 2.1 List All Users
- **Endpoint:** `GET /users`
- **Access:** `SYSTEM_ADMIN`
- **Query Parameters:**
  - `query` (optional string): Search full name or email.
  - `role` (optional enum): Filter by `SYSTEM_ADMIN`, `BUILDING_MANAGER`, `SAFETY_OFFICER`.
  - `active` (optional boolean): Filter active vs inactive accounts.
- **Response (200 OK):** Array of User objects.

### 2.2 Update User Role
- **Endpoint:** `PATCH /users/{id}/role`
- **Access:** `SYSTEM_ADMIN`
- **Request Body:**
```json
{
  "role": "SAFETY_OFFICER"
}
```
- **Response (200 OK):** Updated user entity. (Protected against demoting last active admin).

### 2.3 Toggle User Status
- **Endpoint:** `PATCH /users/{id}/status`
- **Access:** `SYSTEM_ADMIN`
- **Request Body:**
```json
{
  "active": false
}
```
- **Response (200 OK):** Updated user entity. (Protected against deactivating last active admin).

---

## 3. Building Management Endpoints

### 3.1 List Buildings
- **Endpoint:** `GET /buildings`
- **Access:** Authenticated (Role-filtered or all buildings)
- **Query Parameters:** `query`, `status`, `type`
- **Response (200 OK):** Array of Building DTOs with aggregated room and capacity statistics.

### 3.2 Get Building Details
- **Endpoint:** `GET /buildings/{id}`
- **Access:** Authenticated
- **Response (200 OK):** Detailed Building DTO including metadata and operator assignments.

### 3.3 Create Building
- **Endpoint:** `POST /buildings`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Request Body:**
```json
{
  "name": "Nexora Beta Research Lab",
  "buildingCode": "NEX-LAB-02",
  "buildingType": "EDUCATIONAL",
  "address": "400 Campus Drive, Tech City",
  "status": "ACTIVE",
  "description": "High-throughput edge computing and IoT testing campus."
}
```
- **Response (201 Created):** Created Building DTO.

### 3.4 Update Building
- **Endpoint:** `PUT /buildings/{id}`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Response (200 OK):** Updated Building DTO.

### 3.5 Delete Building
- **Endpoint:** `DELETE /buildings/{id}`
- **Access:** `SYSTEM_ADMIN`
- **Response (204 No Content):** Building removed.

### 3.6 Assign Operator to Building
- **Endpoint:** `POST /buildings/{id}/users`
- **Access:** `SYSTEM_ADMIN`
- **Request Body:** `{ "userId": "usr-002" }`
- **Response (200 OK):** Operator assigned to building.

---

## 4. Vertical Floor Management Endpoints

### 4.1 List Floors by Building
- **Endpoint:** `GET /buildings/{buildingId}/floors`
- **Access:** Authenticated
- **Response (200 OK):** Ordered list of floors by `floorNumber`.

### 4.2 Create Floor
- **Endpoint:** `POST /buildings/{buildingId}/floors`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Request Body:**
```json
{
  "floorName": "Level 2 — Engineering Workstations",
  "floorNumber": 2,
  "description": "Software development and AI test harness pods.",
  "layoutWidth": 60.0,
  "layoutDepth": 40.0,
  "status": "ACTIVE"
}
```
- **Response (201 Created):** Created Floor DTO.

### 4.3 Update Floor
- **Endpoint:** `PUT /floors/{floorId}`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Response (200 OK):** Updated Floor DTO.

### 4.4 Delete Floor
- **Endpoint:** `DELETE /floors/{floorId}`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Response (204 No Content):** Floor deleted.

---

## 5. Room & 3D Spatial Management Endpoints

### 5.1 List Rooms by Floor
- **Endpoint:** `GET /floors/{floorId}/rooms`
- **Access:** Authenticated
- **Response (200 OK):** List of rooms with dimensions and coordinates.

### 5.2 Create Room
- **Endpoint:** `POST /floors/{floorId}/rooms`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Request Body:**
```json
{
  "roomName": "Edge AI Cluster Core",
  "roomCode": "204-AIC",
  "roomType": "SERVER_ROOM",
  "description": "Rack servers hosting local ML inference models.",
  "maximumCapacity": 8,
  "currentOccupancy": 2,
  "safetyStatus": "NORMAL",
  "width": 10.0,
  "depth": 8.0,
  "height": 3.5,
  "posX": 25.0,
  "posY": 0.0,
  "posZ": 12.0
}
```
- **Response (201 Created):** Created Room DTO.

### 5.3 Update Room
- **Endpoint:** `PUT /rooms/{roomId}`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Response (200 OK):** Updated Room DTO.

### 5.4 Delete Room
- **Endpoint:** `DELETE /rooms/{roomId}`
- **Access:** `SYSTEM_ADMIN`, `BUILDING_MANAGER`
- **Response (204 No Content):** Room deleted.

---

## 6. System Telemetry & Metrics Endpoints

### 6.1 Dashboard Statistics
- **Endpoint:** `GET /system/dashboard-stats`
- **Access:** Authenticated
- **Description:** Aggregates real-time building, floor, room counts, capacity/occupancy load, and safety zone summary.
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "totalBuildings": 1,
    "totalFloors": 3,
    "totalRooms": 7,
    "totalCapacity": 340,
    "currentOccupancy": 42,
    "occupancyPercentage": 12.35,
    "activeAlerts": 0,
    "safetySummary": {
      "NORMAL": 7,
      "WARNING": 0,
      "CRITICAL": 0
    }
  }
}
```
