-- Phase 1: Architectural Foundation Schema
-- Establishes initial database schema baseline and metadata tracking

CREATE TABLE IF NOT EXISTS system_metadata (
    id VARCHAR(64) PRIMARY KEY,
    meta_key VARCHAR(128) NOT NULL UNIQUE,
    meta_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Seed initial system metadata
INSERT INTO system_metadata (id, meta_key, meta_value, description)
VALUES 
    ('meta-01', 'system_version', '1.0.0-SNAPSHOT', 'Current version of the Smart Building platform'),
    ('meta-02', 'current_phase', 'Phase 1 - Architectural Foundation', 'Active milestone identifier'),
    ('meta-03', 'database_initialization_timestamp', CURRENT_TIMESTAMP::text, 'Timestamp of initial schema creation')
ON CONFLICT (meta_key) DO NOTHING;
