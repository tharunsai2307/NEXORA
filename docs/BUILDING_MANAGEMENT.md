# NEXORA — Building Spatial Hierarchy & Digital Twin Coordinate Architecture

## 1. Executive Summary
The NEXORA Building Management module serves as the structural and spatial backbone for the entire smart building ecosystem. It provides an object-oriented domain hierarchy (**Building $\rightarrow$ Floor $\rightarrow$ Room**) that models physical facilities, stores real-time occupancy metrics, and captures 3D Euclidean spatial coordinates required for Phase 5's interactive Three.js Digital Twin.

---

## 2. Spatial Hierarchy Model

```
+-----------------------------------------------------------------------------------+
|                                    BUILDING                                       |
|  - ID, Name, Building Code (Unique, e.g. "NEX-TWR-01")                            |
|  - Type: COMMERCIAL, RESIDENTIAL, INDUSTRIAL, EDUCATIONAL, HEALTHCARE             |
|  - Status: ACTIVE, MAINTENANCE, INACTIVE, EMERGENCY                              |
|  - Aggregated Metrics: Total Floors, Room Count, Total Capacity, Current Load     |
+------------------------------------------+----------------------------------------+
                                           | 1:N
                                           v
+-----------------------------------------------------------------------------------+
|                                      FLOOR                                        |
|  - ID, Floor Name (e.g. "Ground Lobby", "Level 1 Research Lab")                   |
|  - Floor Number (Unique per Building, e.g. 0, 1, 2)                              |
|  - 2D Footprint: Layout Width (m), Layout Depth (m)                               |
|  - Status: ACTIVE, MAINTENANCE, RESTRICTED, EVACUATING                            |
+------------------------------------------+----------------------------------------+
                                           | 1:N
                                           v
+-----------------------------------------------------------------------------------+
|                                      ROOM                                         |
|  - ID, Room Name, Room Code (Unique per Floor, e.g. "101-CONF", "102-LAB")        |
|  - Classification: CONFERENCE_ROOM, OFFICE, LABORATORY, SERVER_ROOM, etc.         |
|  - Capacity: Maximum Capacity (headcount), Current Occupancy (real-time)          |
|  - Safety Status: NORMAL, WARNING, CRITICAL                                       |
|  - Physical Dimensions (m): Width, Depth, Height                                  |
|  - 3D Coordinates (m): Pos X (Lateral), Pos Y (Elevation), Pos Z (Longitudinal)   |
+-----------------------------------------------------------------------------------+
```

---

## 3. 3D Digital Twin Coordinate System (Phase 5 Alignment)

To ensure seamless integration with **Three.js** and **React Three Fiber (R3F)** in Phase 5 without refactoring backend databases, NEXORA establishes a standardized Euclidean coordinate frame:

| Dimension / Coordinate | Axis | Unit | Description |
|---|:---:|:---:|---|
| **`pos_x`** | $X$ | Meters ($m$) | Lateral offset from the floor's bottom-left local origin |
| **`pos_y`** | $Y$ | Meters ($m$) | Vertical height / elevation relative to the floor plane ($Y = 0$) |
| **`pos_z`** | $Z$ | Meters ($m$) | Longitudinal depth offset from the floor's bottom-left local origin |
| **`width`** | $\Delta X$ | Meters ($m$) | Room width spanning along the lateral $X$-axis |
| **`height`** | $\Delta Y$ | Meters ($m$) | Room ceiling clearance spanning along the vertical $Y$-axis |
| **`depth`** | $\Delta Z$ | Meters ($m$) | Room depth spanning along the longitudinal $Z$-axis |

### Coordinate Mapping Example
```
         +---------------------------------------------+  (layout_width: 60m)
         |                                             |
         |     +----------------+                      |
         |     |  Server Room   |                      |
         |     |  width: 8m     |                      |
(Z-axis) |     |  depth: 6m     |                      |
depth:   |     |  pos_x: 24m    |                      |
 40m     |     |  pos_z: 14m    |                      |
         |     +----------------+                      |
         |                                             |
         +---------------------------------------------+
       (0,0,0) Origin -----------------------------> (X-axis)
```

---

## 4. Room Classifications & Enums

### 4.1 Room Types (`RoomType`)
1. **`OFFICE`** — Private workstations, open-plan workspaces.
2. **`CONFERENCE_ROOM`** — Formal collaboration spaces with smart projectors.
3. **`LABORATORY`** — Edge AI hardware labs, chemistry, and testing facilities.
4. **`SERVER_ROOM`** — High-density compute racks with strict thermal monitoring.
5. **`CLASSROOM`** — Academic lecture halls and training rooms.
6. **`LOBBY`** — Entry reception zones with visitor turnstiles.
7. **`CORRIDOR`** — Internal traversal passageways.
8. **`STAIRCASE`** — Primary vertical egress routes.
9. **`CAFETERIA`** — Dining and refreshment halls.
10. **`STORAGE_ROOM`** — Utility and maintenance supply closets.
11. **`EMERGENCY_EXIT_AREA`** — Evacuation staging zones and fire doors.
12. **`OTHER`** — Unclassified functional spaces.

### 4.2 Safety Statuses (`SafetyStatus`)
- **`NORMAL`** — Nominal environmental readings (CO2, Temp, Smoke, Motion).
- **`WARNING`** — Anomaly detected by Edge AI or threshold reached.
- **`CRITICAL`** — Active safety hazard (fire, gas spike, structural limit exceeded).

---

## 5. Domain Invariants & Integrity Constraints

1. **Building Code Uniqueness:**
   - Every building must possess a globally unique identifier (e.g. `NEX-TWR-01`). Duplicates are rejected with HTTP 409 Conflict.
2. **Floor Number Uniqueness:**
   - Within any single building, floor numbers cannot collide. An attempt to create another Level 1 for `bld-001` is rejected with HTTP 409.
3. **Room Code Uniqueness:**
   - Room codes are scoped per floor (e.g. Floor 1 Room 101). Duplicate codes within the same floor are rejected with HTTP 409.
4. **Dependency Safeguards:**
   - Deleting a building with existing floors or rooms requires explicit confirmation; associated sub-entities are systematically checked or cascaded according to relational rules.
5. **Capacity Invariant:**
   - `maximumCapacity` must be greater than or equal to 0. `currentOccupancy` cannot exceed `maximumCapacity` in nominal operations without triggering an occupancy alert.

