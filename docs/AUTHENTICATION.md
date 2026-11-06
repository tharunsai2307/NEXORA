# NEXORA — Authentication & Role-Based Access Control (RBAC) Architecture

## 1. Executive Summary
The NEXORA Smart Building Platform implements an enterprise-grade, stateless security architecture powered by **Spring Security 6**, **JSON Web Tokens (JWT)**, and **BCrypt password hashing**. The security subsystem provides robust authentication, fine-grained role-based authorization, and comprehensive session management across all application tiers.

---

## 2. Authentication Architecture

### 2.1 Component Flow Diagram

```
+-----------------------------------------------------------------------------------+
|                                 React Frontend                                    |
|  +-------------------+    +----------------------+    +------------------------+  |
|  |     LoginPage     |    |   AuthContext.tsx    |    |  Axios Interceptor     |  |
|  +---------+---------+    +----------+-----------+    +-----------+------------+  |
+------------|-------------------------|----------------------------|---------------+
             |                         |                            |
             | POST /api/v1/auth/login | Saves JWT in localStorage  | Attaches Header:
             |                         | State: user, token         | Bearer <JWT>
             v                         v                            v
+-----------------------------------------------------------------------------------+
|                              Spring Boot Backend                                  |
|                                                                                   |
|  +---------------------------+        +----------------------------------------+  |
|  |   SecurityFilterChain     | -----> |         JwtAuthenticationFilter        |  |
|  +---------------------------+        +-------------------+--------------------+  |
|                                                           |                       |
|                                                           v                       |
|  +---------------------------+        +----------------------------------------+  |
|  |    SecurityContextHolder  | <----- |           JwtTokenProvider             |  |
|  |  (Populated Authentication|        |   - Validates HMAC-SHA256 Signature    |  |
|  |   with ROLE_ Authorities) |        |   - Extracts Claims, Subject, Roles    |  |
|  +---------------------------+        +----------------------------------------+  |
|               |                                                                   |
|               v                                                                   |
|  +---------------------------+        +----------------------------------------+  |
|  |    @PreAuthorize Check    | -----> |           Controller / Service         |  |
|  |  (e.g., hasRole('ADMIN')) |        |          Business Logic Execution      |  |
|  +---------------------------+        +----------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

---

## 3. Cryptographic Standards & Token Specifications

| Parameter | Specification | Description |
|---|---|---|
| **Signature Algorithm** | HMAC-SHA256 (`HS256`) | Cryptographically signed using JJWT `0.12.6` with secure 256-bit secret |
| **Token Format** | RFC 7519 Compliant JWT | Compact URL-safe string structured into Header, Claims Payload, and Signature |
| **Password Hashing** | BCrypt (`PasswordEncoder`) | Work factor: 10 rounds with per-user salt generation |
| **Default Expiration** | 86,400 seconds (24 Hours) | Configurable via `app.jwt.expiration-ms` property |
| **Transport** | HTTP Authorization Header | `Authorization: Bearer <base64-encoded-token>` |

### Sample JWT Claims Payload
```json
{
  "sub": "admin@nexora.com",
  "userId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "fullName": "System Administrator",
  "role": "SYSTEM_ADMIN",
  "iat": 1774886400,
  "exp": 1774972800
}
```

---

## 4. Role Hierarchy & Access Matrix

NEXORA specifies three primary operator personas:

1. **`SYSTEM_ADMIN` (Level 3 - Full Clearance):**
   - Complete platform sovereignty.
   - User directory administration (role promotion/demotion, status toggles).
   - Building topology deletion, operator assignment to facilities.
   - Protected by the **Last Active Admin Invariant** (the last active system admin cannot be deactivated or demoted).

2. **`BUILDING_MANAGER` (Level 2 - Facility Operations):**
   - Default role assigned upon public self-registration.
   - CRUD permissions over assigned buildings, vertical floor hierarchies, and physical rooms.
   - Configures spatial coordinates ($X, Y, Z$) and dimensions for 3D digital twins.

3. **`SAFETY_OFFICER` (Level 1 - Safety & Monitoring):**
   - Read-only audit access to building, floor, and room structural topologies.
   - Dedicated oversight for emergency response, sensor anomalies, and evacuation routing.

### Permissions Matrix

| Module / Operation | HTTP Endpoint | `SYSTEM_ADMIN` | `BUILDING_MANAGER` | `SAFETY_OFFICER` | Public |
|---|---|:---:|:---:|:---:|:---:|
| User Registration | `POST /api/v1/auth/register` | Yes | Yes | Yes | **Yes** |
| User Login | `POST /api/v1/auth/login` | Yes | Yes | Yes | **Yes** |
| User Profile | `GET /api/v1/auth/me` | **Yes** | **Yes** | **Yes** | No |
| User Directory | `GET /api/v1/users` | **Yes** | No | No | No |
| Update User Role | `PATCH /api/v1/users/{id}/role` | **Yes** | No | No | No |
| Toggle User Status | `PATCH /api/v1/users/{id}/status` | **Yes** | No | No | No |
| List Buildings | `GET /api/v1/buildings` | **Yes** | **Yes** | **Yes** | No |
| Create Building | `POST /api/v1/buildings` | **Yes** | **Yes** | No | No |
| Edit Building | `PUT /api/v1/buildings/{id}` | **Yes** | **Yes** | No | No |
| Delete Building | `DELETE /api/v1/buildings/{id}` | **Yes** | No | No | No |
| Assign Operator | `POST /api/v1/buildings/{id}/users` | **Yes** | No | No | No |
| Manage Floors | `/api/v1/buildings/{id}/floors/**` | **Yes** | **Yes** | Read Only | No |
| Manage Rooms | `/api/v1/floors/{id}/rooms/**` | **Yes** | **Yes** | Read Only | No |
| System Telemetry | `GET /api/v1/system/dashboard-stats` | **Yes** | **Yes** | **Yes** | No |

---

## 5. Security Invariants & Defensive Controls

1. **Self-Elevation Prevention:**
   - The public `/auth/register` endpoint strictly enforces `BUILDING_MANAGER` role assignment on all new registrations. The payload cannot override this role.
2. **Last Admin Protection:**
   - Any attempt to demote (`PATCH /users/{id}/role`) or deactivate (`PATCH /users/{id}/status`) a user checks `userRepository.countActiveAdmins()`. If only one active admin exists and targets themselves, the transaction is aborted with `400 Bad Request`.
3. **Password Security:**
   - Passwords must contain a minimum of 8 characters. Plain text passwords are never stored, logged, or serialized into DTO responses.
4. **Stateless Session Management:**
   - `SessionCreationPolicy.STATELESS` ensures that backend nodes do not maintain server-side sessions, supporting horizontal cloud scalability.
5. **Standardized Error Disclosures:**
   - 401 Unauthorized returns a standardized RFC 7807 JSON envelope via `JwtAuthenticationEntryPoint`.
   - 403 Forbidden returns a standardized JSON envelope via `CustomAccessDeniedHandler`.

