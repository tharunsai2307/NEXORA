-- =============================================================================
-- Migration: V6__analytics_and_reporting.sql
-- Description: Advanced Analytics, Reporting & Incident Post-Mortem Schema
-- Target: PostgreSQL 15+ / 16+
-- =============================================================================

-- 1. Incident Post-Mortem Reports Table
CREATE TABLE IF NOT EXISTS incident_post_mortems (
    id VARCHAR(64) PRIMARY KEY,
    incident_id VARCHAR(64) NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    report_title VARCHAR(255) NOT NULL,
    report_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    author_id VARCHAR(64) REFERENCES users(id) ON DELETE SET NULL,
    author_name VARCHAR(128),
    executive_summary TEXT,
    incident_overview TEXT,
    timeline_narrative TEXT,
    sensor_analysis TEXT,
    ai_assessment_summary TEXT,
    response_evaluation TEXT,
    evacuation_summary TEXT,
    root_cause_assessment TEXT,
    corrective_actions TEXT,
    preventive_recommendations TEXT,
    reviewed_by VARCHAR(128),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    finalized_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_postmortem_incident UNIQUE (incident_id)
);

CREATE INDEX IF NOT EXISTS idx_postmortems_incident ON incident_post_mortems(incident_id);
CREATE INDEX IF NOT EXISTS idx_postmortems_status ON incident_post_mortems(report_status);
CREATE INDEX IF NOT EXISTS idx_postmortems_created ON incident_post_mortems(created_at DESC);

-- 2. Generated Reports Catalog Table
CREATE TABLE IF NOT EXISTS generated_reports (
    id VARCHAR(64) PRIMARY KEY,
    report_type VARCHAR(64) NOT NULL,
    building_id VARCHAR(64) REFERENCES buildings(id) ON DELETE CASCADE,
    report_name VARCHAR(255) NOT NULL,
    report_format VARCHAR(16) NOT NULL DEFAULT 'PDF',
    date_range_start TIMESTAMP WITH TIME ZONE,
    date_range_end TIMESTAMP WITH TIME ZONE,
    generated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    summary_metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_generated_reports_building ON generated_reports(building_id);
CREATE INDEX IF NOT EXISTS idx_generated_reports_type ON generated_reports(report_type);
CREATE INDEX IF NOT EXISTS idx_generated_reports_created ON generated_reports(created_at DESC);

