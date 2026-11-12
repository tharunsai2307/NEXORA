# NEXORA — Database Architecture & Schema Specification

**System:** NEXORA — Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System  
**Database Management System:** PostgreSQL 15+ / 16+  
**Schema Migration Engine:** Flyway  
**Current Schema Migration Version:** `V6__analytics_and_reporting.sql`

---

## 1. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    users ||--o{ incidents : "assigned_officer"
    users ||--o{ incident_activities : "performed_by"
    users ||--o{ incident_post_mortems : "author"

    buildings ||--|{ floors : "has_floors"
    buildings ||--o{ sensors : "contains_sensors"
    buildings ||--o{ simulation_sessions : "hosts_sessions"
    buildings ||--o{ incidents : "records_incidents"
    buildings ||--o{ evacuation_nodes : "contains_nodes"
    buildings ||--o{ evacuation_connections : "contains_connections"
    buildings ||--o{ evacuation_sessions : "holds_evacuations"
    buildings ||--o{ evacuation_routes : "provides_routes"
    buildings ||--o{ generated_reports : "documents"

    floors ||--|{ rooms : "divides_into"
    floors ||--o{ evacuation_nodes : "elevates"

    rooms ||--o{ sensors : "monitored_by"
    rooms ||--o{ ai_predictions : "predicted_for"
    rooms ||--o{ room_risk_assessments : "scored_for"
    rooms ||--o{ incidents : "originates_in"
    rooms ||--o{ evacuation_nodes : "egress_source"
    rooms ||--o{ evacuation_routes : "egress_origin"

    sensors ||--o{ sensor_readings : "produces_readings"
    sensors }o--|| simulation_scenarios : "influenced_by"

    simulation_sessions ||--o{ simulation_scenarios : "executes"

    incidents ||--o{ incident_activities : "logs_history"
    incidents ||--o{ emergency_alerts : "triggers"
    incidents ||--o| incident_post_mortems : "post_mortem"

    evacuation_nodes ||--o{ evacuation_connections : "from_node"
    evacuation_nodes ||--o{ evacuation_connections : "to_node"
    evacuation_nodes ||--o{ evacuation_routes : "target_exit"

    evacuation_sessions ||--o{ evacuation_routes : "computes"

    users {
        varchar(64) id PK
        varchar(128) email UK
        varchar(255) password_hash
        varchar(128) full_name
        varchar(32) role
        varchar(32) status
        timestamp created_at
        timestamp updated_at
    }

    buildings {
        varchar(64) id PK
        varchar(128) name
        varchar(64) building_code UK
        varchar(255) address
        varchar(32) building_type
        varchar(32) status
        integer total_floors
        double_precision total_area_sq_ft
        integer total_capacity
        timestamp created_at
        timestamp updated_at
    }

    floors {
        varchar(64) id PK
        varchar(64) building_id FK
        varchar(64) floor_name
        integer floor_number
        double_precision floor_area_sq_ft
        integer room_count
        varchar(32) status
        timestamp created_at
        timestamp updated_at
    }

    rooms {
        varchar(64) id PK
        varchar(64) floor_id FK
        varchar(32) room_number
        varchar(128) room_name
        varchar(32) room_type
        integer maximum_capacity
        integer current_occupancy
        double_precision area_sq_ft
        double_precision position_x
        double_precision position_y
        double_precision position_z
        double_precision dimension_width
        double_precision dimension_height
        double_precision dimension_depth
        varchar(32) safety_status
        timestamp created_at
        timestamp updated_at
    }

    sensors {
        varchar(64) id PK
        varchar(64) room_id FK
        varchar(64) building_id FK
        varchar(64) sensor_code UK
        varchar(128) sensor_name
        varchar(32) sensor_type
        varchar(32) status
        varchar(16) unit
        integer simulation_interval_seconds
        double_precision current_reading
        timestamp last_reading_at
        timestamp created_at
        timestamp updated_at
    }

    sensor_readings {
        bigserial id PK
        varchar(64) sensor_id FK
        double_precision reading_value
        timestamp recorded_at
        varchar(32) quality_flag
    }

    ai_predictions {
        varchar(64) id PK
        varchar(64) room_id FK
        varchar(64) building_id FK
        varchar(32) prediction_type
        double_precision predicted_value
        double_precision confidence_score
        timestamp prediction_timestamp
        timestamp target_timestamp
        text explanation_json
    }

    room_risk_assessments {
        varchar(64) id PK
        varchar(64) room_id FK
        varchar(64) building_id FK
        varchar(32) risk_level
        double_precision overall_risk_score
        double_precision environmental_risk_score
        double_precision occupancy_risk_score
        double_precision anomaly_factor
        boolean critical_override
        text risk_factors_json
        timestamp assessed_at
    }

    incidents {
        varchar(64) id PK
        varchar(64) reference_number UK
        varchar(64) building_id FK
        varchar(64) floor_id FK
        varchar(64) room_id FK
        varchar(32) category
        varchar(32) severity
        varchar(32) status
        varchar(32) detection_source
        varchar(255) title
        text description
        varchar(64) assigned_officer_id FK
        timestamp detected_at
        timestamp acknowledged_at
        timestamp resolved_at
        timestamp closed_at
    }

    evacuation_nodes {
        varchar(64) id PK
        varchar(64) building_id FK
        varchar(64) floor_id FK
        varchar(64) room_id FK
        varchar(128) node_name
        varchar(64) node_code
        varchar(32) node_type
        double_precision pos_x
        double_precision pos_y
        double_precision pos_z
        boolean is_blocked
        boolean is_safe
    }

    evacuation_connections {
        varchar(64) id PK
        varchar(64) building_id FK
        varchar(64) from_node_id FK
        varchar(64) to_node_id FK
        double_precision distance_meters
        boolean is_bidirectional
        boolean is_blocked
        boolean is_accessible
        varchar(32) hazard_level
    }

    evacuation_routes {
        varchar(64) id PK
        varchar(64) session_id FK
        varchar(64) building_id FK
        varchar(64) origin_room_id FK
        varchar(64) target_exit_node_id FK
        varchar(128) target_exit_name
        text path_node_ids_json
        text path_coordinates_json
        double_precision total_distance_meters
        varchar(32) status
        text instructions
        text reason
    }

    incident_post_mortems {
        varchar(64) id PK
        varchar(64) incident_id FK UK
        varchar(255) report_title
        varchar(32) report_status
        varchar(64) author_id FK
        varchar(128) author_name
        text executive_summary
        text incident_overview
        text timeline_narrative
        text sensor_analysis
        text ai_assessment_summary
        text root_cause_assessment
        text corrective_actions
        text preventive_recommendations
        timestamp reviewed_at
        timestamp finalized_at
    }

    generated_reports {
        varchar(64) id PK
        varchar(64) report_type
        varchar(64) building_id FK
        varchar(255) report_name
        varchar(16) report_format
        varchar(128) generated_by
        timestamp created_at
    }
```

---

## 2. Flyway Migration History Ledger

| Migration File | Title & Scope | Tables Created / Altered |
| :--- | :--- | :--- |
| **`V1__init_schema_foundation.sql`** | Core Baseline & Auditing | `system_metadata`, `app_audit_logs`, `flyway_schema_history` |
| **`V2__auth_and_building_management.sql`** | Authentication, RBAC & Topology | `users`, `buildings`, `floors`, `rooms` |
| **`V3__sensor_simulation.sql`** | IoT Sensors & Simulation | `sensors`, `sensor_readings`, `simulation_sessions`, `simulation_scenarios` |
| **`V4__edge_ai_intelligence.sql`** | Edge AI Inferences & Threat Scoring | `ai_model_metadata`, `ai_predictions`, `room_risk_assessments` |
| **`V5__emergency_and_evacuation.sql`** | Emergency Operations & Pathfinding | `incidents`, `incident_activities`, `emergency_alerts`, `evacuation_nodes`, `evacuation_connections`, `evacuation_sessions`, `evacuation_routes` |
| **`V6__analytics_and_reporting.sql`** | Post-Mortem Retrospective & PDF Catalog | `incident_post_mortems`, `generated_reports` |

---

## 3. Relational Integrity, Cascades & Optimization

1. **Foreign Key Integrity:**
   - Deleting a `Building` cascades (`ON DELETE CASCADE`) to its associated `Floors`, `Rooms`, `Sensors`, `EvacuationNodes`, `EvacuationConnections`, and `Incidents`.
   - Deleting an `Incident` cascades to its `IncidentActivities`, `EmergencyAlerts`, and `IncidentPostMortem`.
   - Deleting a `User` cascades safely: assigned incidents and authored post-mortems preserve history using `ON DELETE SET NULL`.
2. **Indexing Strategy:**
   - High-cardinality foreign keys (`sensor_id`, `room_id`, `building_id`) have explicit B-tree indexes.
   - Time-series queries on `sensor_readings(sensor_id, recorded_at DESC)` leverage composite indexing for sub-10ms window aggregation.
   - Spatial graph traversal (`evacuation_connections(building_id, from_node_id, to_node_id)`) uses covering indexes for Dijkstra graph construction.
3. **Storage & Partitioning Readiness:**
   - `sensor_readings` uses 64-bit `BIGSERIAL` auto-incrementing surrogate keys, designed for time-range partitioning in multi-year deployments.
