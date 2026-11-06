-- Phase 2: Authentication & Building Management Schema
-- Establishes Users, Roles, Buildings, Floors, Rooms, Assignments, and Audit Logs

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    role VARCHAR(32) NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 2. Buildings Table
CREATE TABLE IF NOT EXISTS buildings (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    building_code VARCHAR(64) NOT NULL UNIQUE,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    building_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_buildings_code ON buildings(building_code);
CREATE INDEX IF NOT EXISTS idx_buildings_status ON buildings(status);

-- 3. Building-User Assignments Table (Multi-tenancy / Assignment scoping)
CREATE TABLE IF NOT EXISTS building_user_assignments (
    id VARCHAR(36) PRIMARY KEY,
    building_id VARCHAR(36) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_building_user UNIQUE (building_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_assignments_user ON building_user_assignments(user_id);
CREATE INDEX IF NOT EXISTS idx_assignments_building ON building_user_assignments(building_id);

-- 4. Floors Table
CREATE TABLE IF NOT EXISTS floors (
    id VARCHAR(36) PRIMARY KEY,
    building_id VARCHAR(36) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    floor_name VARCHAR(255) NOT NULL,
    floor_number INT NOT NULL,
    description TEXT,
    layout_width DOUBLE PRECISION DEFAULT 60.0 NOT NULL,
    layout_depth DOUBLE PRECISION DEFAULT 40.0 NOT NULL,
    status VARCHAR(32) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_building_floor_number UNIQUE (building_id, floor_number)
);

CREATE INDEX IF NOT EXISTS idx_floors_building ON floors(building_id);

-- 5. Rooms Table (Physical parameters & 3D coordinate foundations for Phase 5 Digital Twin)
CREATE TABLE IF NOT EXISTS rooms (
    id VARCHAR(36) PRIMARY KEY,
    floor_id VARCHAR(36) NOT NULL REFERENCES floors(id) ON DELETE CASCADE,
    room_name VARCHAR(255) NOT NULL,
    room_code VARCHAR(64) NOT NULL,
    room_type VARCHAR(32) NOT NULL,
    description TEXT,
    maximum_capacity INT NOT NULL,
    current_occupancy INT DEFAULT 0 NOT NULL,
    safety_status VARCHAR(32) DEFAULT 'NORMAL' NOT NULL,
    pos_x DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    pos_y DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    pos_z DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    width DOUBLE PRECISION DEFAULT 10.0 NOT NULL,
    depth DOUBLE PRECISION DEFAULT 8.0 NOT NULL,
    height DOUBLE PRECISION DEFAULT 3.5 NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_floor_room_code UNIQUE (floor_id, room_code)
);

CREATE INDEX IF NOT EXISTS idx_rooms_floor ON rooms(floor_id);
CREATE INDEX IF NOT EXISTS idx_rooms_safety_status ON rooms(safety_status);

-- 6. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- 7. Seed Initial Baseline Users (BCrypt hashed with cost factor 10)
-- Passwords:
-- admin@nexora.com   -> Admin@Nexora2026!
-- manager@nexora.com -> Manager@Nexora2026!
-- safety@nexora.com  -> Safety@Nexora2026!
INSERT INTO users (id, full_name, email, password_hash, phone_number, role, active, created_at, updated_at)
VALUES 
    ('u-admin-001', 'System Administrator', 'admin@nexora.com', '$2a$10$kjiWMPBPvjsUJIvbbg6aSOmzRJHSBCR64r/w9o/WJEs4OH9LQfUsS', '+1-555-0100', 'SYSTEM_ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('u-mgr-002', 'Facilities Manager', 'manager@nexora.com', '$2a$10$c.tY0OH2WqMLFOKxUZf0lebeh33Y9wp8n72r0AlMkA9MojeodKMzW', '+1-555-0101', 'BUILDING_MANAGER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('u-sft-003', 'Chief Safety Officer', 'safety@nexora.com', '$2a$10$R8FFdqD.cPyiA2/exzBF5e6F92xl1ZFLtIt3XbXGOhpgTz3mbOmgq', '+1-555-0102', 'SAFETY_OFFICER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- 8. Seed Reference Flagship Building ("NEXORA Innovation Tower")
INSERT INTO buildings (id, name, building_code, description, address, building_type, status, created_at, updated_at)
VALUES 
    ('b-tower-001', 'NEXORA Innovation Tower', 'NEX-TWR-01', 'Flagship smart commercial research and innovation facility with multi-zone environmental sensors.', '100 Silicon Boulevard, Tech District', 'COMMERCIAL', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (building_code) DO NOTHING;

-- Assign Building Manager to the flagship building
INSERT INTO building_user_assignments (id, building_id, user_id, assigned_at)
VALUES 
    ('bua-001', 'b-tower-001', 'u-mgr-002', CURRENT_TIMESTAMP),
    ('bua-002', 'b-tower-001', 'u-sft-003', CURRENT_TIMESTAMP)
ON CONFLICT (building_id, user_id) DO NOTHING;

-- Seed Floors for Flagship Building
INSERT INTO floors (id, building_id, floor_name, floor_number, description, layout_width, layout_depth, status, created_at, updated_at)
VALUES 
    ('fl-001', 'b-tower-001', 'Ground Floor (Lobby & Amenities)', 0, 'Main entrance reception, cafeteria, security checkpoint, and conference atrium.', 70.0, 50.0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('fl-002', 'b-tower-001', 'First Floor (Offices & Labs)', 1, 'Engineering workstations, software development labs, and collaborative team zones.', 70.0, 50.0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('fl-003', 'b-tower-001', 'Second Floor (Executive & Data Center)', 2, 'Executive boardrooms, server datacenter room, and network operations center.', 70.0, 50.0, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (building_id, floor_number) DO NOTHING;

-- Seed Sample Rooms for Ground Floor (Floor 0)
INSERT INTO rooms (id, floor_id, room_name, room_code, room_type, description, maximum_capacity, current_occupancy, safety_status, pos_x, pos_y, pos_z, width, depth, height, active, created_at, updated_at)
VALUES 
    ('rm-101', 'fl-001', 'Main Reception & Atrium', 'L0-ATRIUM', 'LOBBY', 'Central ground-floor atrium with visitor registration kiosks.', 80, 0, 'NORMAL', 0.0, 0.0, 0.0, 20.0, 15.0, 4.5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('rm-102', 'fl-001', 'Central Conference Hall', 'L0-CONF-A', 'CONFERENCE_ROOM', 'High-capacity multimedia presentation hall.', 50, 0, 'NORMAL', 25.0, 0.0, 0.0, 15.0, 12.0, 3.8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('rm-103', 'fl-001', 'Cafeteria & Lounge', 'L0-CAFE', 'CAFETERIA', 'Dining area with kitchen facilities and breakout seating.', 60, 0, 'NORMAL', -25.0, 0.0, 0.0, 18.0, 14.0, 3.8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (floor_id, room_code) DO NOTHING;

-- Seed Sample Rooms for First Floor (Floor 1)
INSERT INTO rooms (id, floor_id, room_name, room_code, room_type, description, maximum_capacity, current_occupancy, safety_status, pos_x, pos_y, pos_z, width, depth, height, active, created_at, updated_at)
VALUES 
    ('rm-201', 'fl-002', 'AI & Robotics Lab', 'L1-LAB-AI', 'LABORATORY', 'Embedded hardware testing and edge computing research facility.', 30, 0, 'NORMAL', -15.0, 0.0, 4.0, 14.0, 12.0, 3.5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('rm-202', 'fl-002', 'Open Workspace Alpha', 'L1-WORK-A', 'OFFICE', 'Collaborative agile software development open office.', 45, 0, 'NORMAL', 15.0, 0.0, 4.0, 18.0, 14.0, 3.5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (floor_id, room_code) DO NOTHING;

-- Seed Sample Rooms for Second Floor (Floor 2)
INSERT INTO rooms (id, floor_id, room_name, room_code, room_type, description, maximum_capacity, current_occupancy, safety_status, pos_x, pos_y, pos_z, width, depth, height, active, created_at, updated_at)
VALUES 
    ('rm-301', 'fl-003', 'Primary Datacenter & Server Room', 'L2-SRV-01', 'SERVER_ROOM', 'High-density rack server room with dedicated precision cooling.', 8, 0, 'NORMAL', 0.0, 0.0, 8.0, 12.0, 10.0, 3.5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('rm-302', 'fl-003', 'Executive Boardroom', 'L2-EXEC-01', 'CONFERENCE_ROOM', 'Boardroom equipped with telepresence and spatial acoustic panels.', 20, 0, 'NORMAL', 20.0, 0.0, 8.0, 12.0, 10.0, 3.5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (floor_id, room_code) DO NOTHING;

