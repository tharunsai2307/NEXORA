-- =============================================================================
-- Migration: V5__emergency_and_evacuation.sql
-- Description: Emergency Management & Dynamic Evacuation Routing Schema
-- Target: PostgreSQL 15+ / 16+
-- =============================================================================

-- 1. Incidents Table
CREATE TABLE IF NOT EXISTS incidents (
    id VARCHAR(64) PRIMARY KEY,
    reference_number VARCHAR(64) NOT NULL UNIQUE,
    building_id VARCHAR(64) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    floor_id VARCHAR(64) REFERENCES floors(id) ON DELETE SET NULL,
    room_id VARCHAR(64) REFERENCES rooms(id) ON DELETE SET NULL,
    category VARCHAR(32) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DETECTED',
    detection_source VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    assigned_user_id VARCHAR(64) REFERENCES users(id) ON DELETE SET NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_incidents_building ON incidents(building_id);
CREATE INDEX IF NOT EXISTS idx_incidents_room ON incidents(room_id);
CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents(status);
CREATE INDEX IF NOT EXISTS idx_incidents_severity ON incidents(severity);
CREATE INDEX IF NOT EXISTS idx_incidents_detected_at ON incidents(detected_at DESC);

-- 2. Incident Activity Log
CREATE TABLE IF NOT EXISTS incident_activities (
    id VARCHAR(64) PRIMARY KEY,
    incident_id VARCHAR(64) NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    action VARCHAR(64) NOT NULL,
    details TEXT,
    performed_by VARCHAR(64) NOT NULL DEFAULT 'SYSTEM',
    performed_by_name VARCHAR(128),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_incident_activities_incident ON incident_activities(incident_id, created_at DESC);

-- 3. Emergency Alerts
CREATE TABLE IF NOT EXISTS emergency_alerts (
    id VARCHAR(64) PRIMARY KEY,
    incident_id VARCHAR(64) REFERENCES incidents(id) ON DELETE CASCADE,
    building_id VARCHAR(64) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    alert_type VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    headline VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    broadcast_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_emergency_alerts_building ON emergency_alerts(building_id, broadcast_at DESC);

-- 4. Evacuation Graph: Nodes
CREATE TABLE IF NOT EXISTS evacuation_nodes (
    id VARCHAR(64) PRIMARY KEY,
    building_id VARCHAR(64) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    floor_id VARCHAR(64) REFERENCES floors(id) ON DELETE CASCADE,
    room_id VARCHAR(64) REFERENCES rooms(id) ON DELETE SET NULL,
    node_name VARCHAR(128) NOT NULL,
    node_code VARCHAR(64) NOT NULL,
    node_type VARCHAR(32) NOT NULL,
    pos_x DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    pos_y DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    pos_z DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_safe BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_building_node_code UNIQUE (building_id, node_code)
);

CREATE INDEX IF NOT EXISTS idx_evacuation_nodes_building ON evacuation_nodes(building_id);
CREATE INDEX IF NOT EXISTS idx_evacuation_nodes_floor ON evacuation_nodes(floor_id);
CREATE INDEX IF NOT EXISTS idx_evacuation_nodes_room ON evacuation_nodes(room_id);
CREATE INDEX IF NOT EXISTS idx_evacuation_nodes_type ON evacuation_nodes(node_type);

-- 5. Evacuation Graph: Connections (Edges)
CREATE TABLE IF NOT EXISTS evacuation_connections (
    id VARCHAR(64) PRIMARY KEY,
    building_id VARCHAR(64) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    from_node_id VARCHAR(64) NOT NULL REFERENCES evacuation_nodes(id) ON DELETE CASCADE,
    to_node_id VARCHAR(64) NOT NULL REFERENCES evacuation_nodes(id) ON DELETE CASCADE,
    distance_meters DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    hazard_level VARCHAR(32) NOT NULL DEFAULT 'NONE',
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    is_accessible BOOLEAN NOT NULL DEFAULT TRUE,
    bidirectional BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evacuation_conn_building ON evacuation_connections(building_id);
CREATE INDEX IF NOT EXISTS idx_evacuation_conn_from ON evacuation_connections(from_node_id);
CREATE INDEX IF NOT EXISTS idx_evacuation_conn_to ON evacuation_connections(to_node_id);

-- 6. Evacuation Sessions
CREATE TABLE IF NOT EXISTS evacuation_sessions (
    id VARCHAR(64) PRIMARY KEY,
    incident_id VARCHAR(64) REFERENCES incidents(id) ON DELETE SET NULL,
    building_id VARCHAR(64) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    session_status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
    session_name VARCHAR(255) NOT NULL,
    initiated_by VARCHAR(64) NOT NULL DEFAULT 'SYSTEM',
    notes TEXT,
    total_rooms INT NOT NULL DEFAULT 0,
    cleared_rooms INT NOT NULL DEFAULT 0,
    evacuating_rooms INT NOT NULL DEFAULT 0,
    blocked_rooms INT NOT NULL DEFAULT 0,
    total_initial_occupants INT NOT NULL DEFAULT 0,
    remaining_occupants INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evacuation_sessions_building ON evacuation_sessions(building_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_evacuation_sessions_status ON evacuation_sessions(session_status);

-- 7. Evacuation Room Status Tracking
CREATE TABLE IF NOT EXISTS evacuation_room_statuses (
    id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL REFERENCES evacuation_sessions(id) ON DELETE CASCADE,
    room_id VARCHAR(64) NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
    initial_occupancy INT NOT NULL DEFAULT 0,
    remaining_occupants INT NOT NULL DEFAULT 0,
    occupants_needing_assistance INT NOT NULL DEFAULT 0,
    cleared_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_session_room UNIQUE (session_id, room_id)
);

CREATE INDEX IF NOT EXISTS idx_evac_room_status_session ON evacuation_room_statuses(session_id);
CREATE INDEX IF NOT EXISTS idx_evac_room_status_room ON evacuation_room_statuses(room_id);

-- 8. Calculated Evacuation Routes
CREATE TABLE IF NOT EXISTS evacuation_routes (
    id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) REFERENCES evacuation_sessions(id) ON DELETE CASCADE,
    building_id VARCHAR(64) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    origin_room_id VARCHAR(64) NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    origin_node_id VARCHAR(64) NOT NULL REFERENCES evacuation_nodes(id) ON DELETE CASCADE,
    destination_exit_id VARCHAR(64) REFERENCES evacuation_nodes(id) ON DELETE SET NULL,
    destination_exit_name VARCHAR(128),
    ordered_node_ids TEXT NOT NULL,
    ordered_coordinates TEXT NOT NULL,
    total_distance_meters DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    route_status VARCHAR(32) NOT NULL DEFAULT 'SAFE',
    floor_transitions TEXT,
    calculation_reason TEXT,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evacuation_routes_building ON evacuation_routes(building_id);
CREATE INDEX IF NOT EXISTS idx_evacuation_routes_room ON evacuation_routes(origin_room_id);

-- 9. Seed Evacuation Graph Nodes for Flagship Building (b-tower-001)
INSERT INTO evacuation_nodes (
    id, building_id, floor_id, room_id, node_name, node_code, node_type, pos_x, pos_y, pos_z, is_safe, is_blocked
) VALUES
    -- Floor 0 (Ground) Room Nodes
    ('node-rm-101', 'b-tower-001', 'fl-001', 'rm-101', 'Atrium Egress Node', 'NODE-L0-101', 'ROOM', 0.0, 0.0, 0.0, TRUE, FALSE),
    ('node-rm-102', 'b-tower-001', 'fl-001', 'rm-102', 'Conf Hall Egress Node', 'NODE-L0-102', 'ROOM', 25.0, 0.0, 0.0, TRUE, FALSE),
    ('node-rm-103', 'b-tower-001', 'fl-001', 'rm-103', 'Cafeteria Egress Node', 'NODE-L0-103', 'ROOM', -25.0, 0.0, 0.0, TRUE, FALSE),
    -- Floor 0 Corridors, Stairs & Exits
    ('node-corr-l0', 'b-tower-001', 'fl-001', NULL, 'Ground Floor Central Concourse', 'NODE-L0-CORR', 'CORRIDOR', 0.0, 0.0, -8.0, TRUE, FALSE),
    ('node-stair-l0', 'b-tower-001', 'fl-001', NULL, 'Stairwell A (Ground Landing)', 'NODE-L0-STAIR', 'STAIRCASE', -10.0, 0.0, -12.0, TRUE, FALSE),
    ('node-exit-main', 'b-tower-001', 'fl-001', NULL, 'North Emergency Egress Plaza', 'NODE-EXIT-MAIN', 'EMERGENCY_EXIT', 0.0, 0.0, -18.0, TRUE, FALSE),
    ('node-exit-east', 'b-tower-001', 'fl-001', NULL, 'East Fire Escape Door', 'NODE-EXIT-EAST', 'EMERGENCY_EXIT', 32.0, 0.0, -5.0, TRUE, FALSE),

    -- Floor 1 Room Nodes
    ('node-rm-201', 'b-tower-001', 'fl-002', 'rm-201', 'Robotics Lab Egress Node', 'NODE-L1-201', 'ROOM', -15.0, 0.0, 4.0, TRUE, FALSE),
    ('node-rm-202', 'b-tower-001', 'fl-002', 'rm-202', 'Workspace Alpha Egress Node', 'NODE-L1-202', 'ROOM', 15.0, 0.0, 4.0, TRUE, FALSE),
    -- Floor 1 Corridor & Stairs
    ('node-corr-l1', 'b-tower-001', 'fl-002', NULL, 'Level 1 Hallway Spine', 'NODE-L1-CORR', 'CORRIDOR', 0.0, 0.0, -4.0, TRUE, FALSE),
    ('node-stair-l1', 'b-tower-001', 'fl-002', NULL, 'Stairwell A (Level 1 Landing)', 'NODE-L1-STAIR', 'STAIRCASE', -10.0, 0.0, -12.0, TRUE, FALSE),

    -- Floor 2 Room Nodes
    ('node-rm-301', 'b-tower-001', 'fl-003', 'rm-301', 'Datacenter Egress Node', 'NODE-L2-301', 'ROOM', 0.0, 0.0, 8.0, TRUE, FALSE),
    ('node-rm-302', 'b-tower-001', 'fl-003', 'rm-302', 'Boardroom Egress Node', 'NODE-L2-302', 'ROOM', 20.0, 0.0, 8.0, TRUE, FALSE),
    -- Floor 2 Corridor & Stairs
    ('node-corr-l2', 'b-tower-001', 'fl-003', NULL, 'Level 2 Executive Corridor', 'NODE-L2-CORR', 'CORRIDOR', 0.0, 0.0, 0.0, TRUE, FALSE),
    ('node-stair-l2', 'b-tower-001', 'fl-003', NULL, 'Stairwell A (Level 2 Landing)', 'NODE-L2-STAIR', 'STAIRCASE', -10.0, 0.0, -12.0, TRUE, FALSE)
ON CONFLICT (building_id, node_code) DO NOTHING;

-- 10. Seed Evacuation Connections for Flagship Building (b-tower-001)
INSERT INTO evacuation_connections (
    id, building_id, from_node_id, to_node_id, distance_meters, hazard_level, is_blocked, is_accessible, bidirectional
) VALUES
    -- Floor 0 Connections
    ('conn-001', 'b-tower-001', 'node-rm-101', 'node-corr-l0', 8.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-002', 'b-tower-001', 'node-rm-102', 'node-corr-l0', 26.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-003', 'b-tower-001', 'node-rm-103', 'node-corr-l0', 26.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-004', 'b-tower-001', 'node-corr-l0', 'node-stair-l0', 11.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-005', 'b-tower-001', 'node-corr-l0', 'node-exit-main', 10.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-006', 'b-tower-001', 'node-rm-102', 'node-exit-east', 9.0, 'NONE', FALSE, TRUE, TRUE),

    -- Floor 1 Connections
    ('conn-007', 'b-tower-001', 'node-rm-201', 'node-corr-l1', 17.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-008', 'b-tower-001', 'node-rm-202', 'node-corr-l1', 17.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-009', 'b-tower-001', 'node-corr-l1', 'node-stair-l1', 13.0, 'NONE', FALSE, TRUE, TRUE),

    -- Floor 2 Connections
    ('conn-010', 'b-tower-001', 'node-rm-301', 'node-corr-l2', 8.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-011', 'b-tower-001', 'node-rm-302', 'node-corr-l2', 21.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-012', 'b-tower-001', 'node-corr-l2', 'node-stair-l2', 16.0, 'NONE', FALSE, TRUE, TRUE),

    -- Vertical Inter-Floor Stairwell Links
    ('conn-013', 'b-tower-001', 'node-stair-l2', 'node-stair-l1', 8.0, 'NONE', FALSE, TRUE, TRUE),
    ('conn-014', 'b-tower-001', 'node-stair-l1', 'node-stair-l0', 8.0, 'NONE', FALSE, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

