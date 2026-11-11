-- Phase 3: IoT Sensor Simulation & Real-Time Monitoring Schema
-- Establishes Sensors, Sensor Readings, Simulation Sessions, and Simulation Scenarios

-- 1. Sensors Table
CREATE TABLE IF NOT EXISTS sensors (
    id VARCHAR(36) PRIMARY KEY,
    room_id VARCHAR(36) NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    sensor_code VARCHAR(64) NOT NULL UNIQUE,
    sensor_name VARCHAR(255) NOT NULL,
    sensor_type VARCHAR(32) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    status VARCHAR(32) DEFAULT 'ACTIVE' NOT NULL,
    simulation_interval_seconds INT DEFAULT 5 NOT NULL,
    last_reading_value DOUBLE PRECISION,
    last_reading_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_room_sensor_type UNIQUE (room_id, sensor_type)
);

CREATE INDEX IF NOT EXISTS idx_sensors_room ON sensors(room_id);
CREATE INDEX IF NOT EXISTS idx_sensors_type ON sensors(sensor_type);
CREATE INDEX IF NOT EXISTS idx_sensors_status ON sensors(status);

-- 2. Sensor Readings Table (Timeseries / historical telemetry store)
CREATE TABLE IF NOT EXISTS sensor_readings (
    id VARCHAR(36) PRIMARY KEY,
    sensor_id VARCHAR(36) NOT NULL REFERENCES sensors(id) ON DELETE CASCADE,
    reading_value DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    reading_source VARCHAR(32) DEFAULT 'SIMULATED' NOT NULL,
    scenario_id VARCHAR(36)
);

CREATE INDEX IF NOT EXISTS idx_readings_sensor_time ON sensor_readings(sensor_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_readings_recorded_at ON sensor_readings(recorded_at DESC);

-- 3. Simulation Sessions Table
CREATE TABLE IF NOT EXISTS simulation_sessions (
    id VARCHAR(36) PRIMARY KEY,
    building_id VARCHAR(36) NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    session_name VARCHAR(255) NOT NULL,
    started_by VARCHAR(36) REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(32) DEFAULT 'RUNNING' NOT NULL,
    scenario_type VARCHAR(64) DEFAULT 'NORMAL' NOT NULL,
    simulation_interval_seconds INT DEFAULT 5 NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    paused_at TIMESTAMP WITH TIME ZONE,
    stopped_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_sim_sessions_bld ON simulation_sessions(building_id);
CREATE INDEX IF NOT EXISTS idx_sim_sessions_status ON simulation_sessions(status);

-- 4. Simulation Scenarios Table
CREATE TABLE IF NOT EXISTS simulation_scenarios (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL REFERENCES simulation_sessions(id) ON DELETE CASCADE,
    scenario_name VARCHAR(255) NOT NULL,
    scenario_type VARCHAR(64) NOT NULL,
    target_configuration TEXT,
    duration_seconds INT DEFAULT 300 NOT NULL,
    status VARCHAR(32) DEFAULT 'ACTIVE' NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_sim_scenarios_session ON simulation_scenarios(session_id);
CREATE INDEX IF NOT EXISTS idx_sim_scenarios_status ON simulation_scenarios(status);

-- 5. Seed Reference Virtual Sensor Suites for Flagship Building (b-tower-001)
-- Ground Floor: rm-101 (Main Reception), rm-102 (Conference Hall), rm-103 (Cafeteria)
-- First Floor: rm-201 (AI & Robotics Lab), rm-202 (Open Workspace Alpha)
-- Second Floor: rm-301 (Server Datacenter), rm-302 (Executive Boardroom)

INSERT INTO sensors (id, room_id, sensor_code, sensor_name, sensor_type, unit, status, simulation_interval_seconds, last_reading_value, last_reading_at, created_at, updated_at)
VALUES
    -- rm-101: Main Reception & Atrium
    ('sns-101-temp', 'rm-101', 'SNS-L0-101-TMP', 'Atrium Ambient Temperature Sensor', 'TEMPERATURE', 'CELSIUS', 'ACTIVE', 5, 22.4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-101-hum',  'rm-101', 'SNS-L0-101-HUM', 'Atrium Relative Humidity Sensor',  'HUMIDITY',    'PERCENT', 'ACTIVE', 5, 48.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-101-smk',  'rm-101', 'SNS-L0-101-SMK', 'Atrium Optical Smoke Detector (Simulated)', 'SMOKE', 'PPM', 'ACTIVE', 5, 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-101-aqi',  'rm-101', 'SNS-L0-101-AQI', 'Atrium Air Quality Index Sensor (Simulated)', 'AIR_QUALITY', 'AQI', 'ACTIVE', 5, 42.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-101-occ',  'rm-101', 'SNS-L0-101-OCC', 'Atrium Optical Flow Occupancy Counter', 'OCCUPANCY', 'PEOPLE', 'ACTIVE', 5, 12.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- rm-102: Central Conference Hall
    ('sns-102-temp', 'rm-102', 'SNS-L0-102-TMP', 'Conference A Temperature Sensor', 'TEMPERATURE', 'CELSIUS', 'ACTIVE', 5, 21.8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-102-hum',  'rm-102', 'SNS-L0-102-HUM', 'Conference A Humidity Sensor',    'HUMIDITY',    'PERCENT', 'ACTIVE', 5, 45.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-102-smk',  'rm-102', 'SNS-L0-102-SMK', 'Conference A Smoke Sensor (Simulated)', 'SMOKE', 'PPM', 'ACTIVE', 5, 0.02, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-102-aqi',  'rm-102', 'SNS-L0-102-AQI', 'Conference A AQI Monitor (Simulated)', 'AIR_QUALITY', 'AQI', 'ACTIVE', 5, 38.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-102-occ',  'rm-102', 'SNS-L0-102-OCC', 'Conference A Occupancy Sensor', 'OCCUPANCY', 'PEOPLE', 'ACTIVE', 5, 18.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- rm-103: Cafeteria & Lounge
    ('sns-103-temp', 'rm-103', 'SNS-L0-103-TMP', 'Cafeteria Temperature Sensor', 'TEMPERATURE', 'CELSIUS', 'ACTIVE', 5, 23.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-103-hum',  'rm-103', 'SNS-L0-103-HUM', 'Cafeteria Humidity Sensor',    'HUMIDITY',    'PERCENT', 'ACTIVE', 5, 52.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-103-smk',  'rm-103', 'SNS-L0-103-SMK', 'Cafeteria Kitchen Smoke Sensor (Simulated)', 'SMOKE', 'PPM', 'ACTIVE', 5, 0.04, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-103-aqi',  'rm-103', 'SNS-L0-103-AQI', 'Cafeteria Air Quality Sensor (Simulated)', 'AIR_QUALITY', 'AQI', 'ACTIVE', 5, 55.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-103-occ',  'rm-103', 'SNS-L0-103-OCC', 'Cafeteria Occupancy Tracker', 'OCCUPANCY', 'PEOPLE', 'ACTIVE', 5, 24.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- rm-201: AI & Robotics Lab
    ('sns-201-temp', 'rm-201', 'SNS-L1-201-TMP', 'Robotics Lab Thermal Sensor', 'TEMPERATURE', 'CELSIUS', 'ACTIVE', 5, 22.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-201-hum',  'rm-201', 'SNS-L1-201-HUM', 'Robotics Lab Humidity Sensor', 'HUMIDITY',    'PERCENT', 'ACTIVE', 5, 42.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-201-smk',  'rm-201', 'SNS-L1-201-SMK', 'Robotics Lab Smoke Sensor (Simulated)', 'SMOKE', 'PPM', 'ACTIVE', 5, 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-201-aqi',  'rm-201', 'SNS-L1-201-AQI', 'Robotics Lab AQI Sensor (Simulated)', 'AIR_QUALITY', 'AQI', 'ACTIVE', 5, 35.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-201-occ',  'rm-201', 'SNS-L1-201-OCC', 'Robotics Lab Occupancy Sensor', 'OCCUPANCY', 'PEOPLE', 'ACTIVE', 5, 8.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- rm-202: Open Workspace Alpha
    ('sns-202-temp', 'rm-202', 'SNS-L1-202-TMP', 'Workspace Alpha Temperature Sensor', 'TEMPERATURE', 'CELSIUS', 'ACTIVE', 5, 22.8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-202-hum',  'rm-202', 'SNS-L1-202-HUM', 'Workspace Alpha Humidity Sensor',    'HUMIDITY',    'PERCENT', 'ACTIVE', 5, 46.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-202-smk',  'rm-202', 'SNS-L1-202-SMK', 'Workspace Alpha Smoke Sensor (Simulated)', 'SMOKE', 'PPM', 'ACTIVE', 5, 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-202-aqi',  'rm-202', 'SNS-L1-202-AQI', 'Workspace Alpha AQI Monitor (Simulated)', 'AIR_QUALITY', 'AQI', 'ACTIVE', 5, 40.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-202-occ',  'rm-202', 'SNS-L1-202-OCC', 'Workspace Alpha Occupancy Counter', 'OCCUPANCY', 'PEOPLE', 'ACTIVE', 5, 22.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- rm-301: Primary Datacenter & Server Room
    ('sns-301-temp', 'rm-301', 'SNS-L2-301-TMP', 'Datacenter Rack Temperature Sensor', 'TEMPERATURE', 'CELSIUS', 'ACTIVE', 5, 19.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-301-hum',  'rm-301', 'SNS-L2-301-HUM', 'Datacenter Precision Humidity Sensor', 'HUMIDITY', 'PERCENT', 'ACTIVE', 5, 38.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-301-smk',  'rm-301', 'SNS-L2-301-SMK', 'Datacenter VESDA Smoke Sensor (Simulated)', 'SMOKE', 'PPM', 'ACTIVE', 5, 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-301-aqi',  'rm-301', 'SNS-L2-301-AQI', 'Datacenter Air Particulate AQI (Simulated)', 'AIR_QUALITY', 'AQI', 'ACTIVE', 5, 22.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-301-occ',  'rm-301', 'SNS-L2-301-OCC', 'Datacenter Biometric Entry Occupancy Sensor', 'OCCUPANCY', 'PEOPLE', 'ACTIVE', 5, 1.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- rm-302: Executive Boardroom
    ('sns-302-temp', 'rm-302', 'SNS-L2-302-TMP', 'Boardroom Ambient Temperature Sensor', 'TEMPERATURE', 'CELSIUS', 'ACTIVE', 5, 21.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-302-hum',  'rm-302', 'SNS-L2-302-HUM', 'Boardroom Humidity Sensor',             'HUMIDITY',    'PERCENT', 'ACTIVE', 5, 44.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-302-smk',  'rm-302', 'SNS-L2-302-SMK', 'Boardroom Smoke Sensor (Simulated)',    'SMOKE',       'PPM',     'ACTIVE', 5, 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-302-aqi',  'rm-302', 'SNS-L2-302-AQI', 'Boardroom Air Quality Sensor (Simulated)', 'AIR_QUALITY', 'AQI', 'ACTIVE', 5, 30.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sns-302-occ',  'rm-302', 'SNS-L2-302-OCC', 'Boardroom Smart Occupancy Sensor',     'OCCUPANCY',   'PEOPLE',  'ACTIVE', 5, 6.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (sensor_code) DO NOTHING;

-- Seed initial baseline readings for each sensor
INSERT INTO sensor_readings (id, sensor_id, reading_value, recorded_at, reading_source)
SELECT
    'rd-' || id,
    id,
    last_reading_value,
    CURRENT_TIMESTAMP,
    'SIMULATED'
FROM sensors
ON CONFLICT DO NOTHING;
