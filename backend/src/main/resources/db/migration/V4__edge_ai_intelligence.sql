-- =============================================================================
-- Migration: V4__edge_ai_intelligence.sql
-- Description: Edge AI Intelligence Schema (Anomaly Detection, Occupancy Prediction,
--              Risk Assessment, and AI Model Metadata)
-- Target: PostgreSQL 15+ / 16+
-- =============================================================================

-- 1. AI Predictions Table
CREATE TABLE IF NOT EXISTS ai_predictions (
    id VARCHAR(64) PRIMARY KEY,
    room_id VARCHAR(64) NOT NULL,
    prediction_type VARCHAR(64) NOT NULL,
    predicted_value DOUBLE PRECISION NOT NULL,
    prediction_horizon_minutes INT NOT NULL DEFAULT 15,
    confidence_or_score DOUBLE PRECISION,
    model_version VARCHAR(64) NOT NULL,
    prediction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_predictions_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ai_predictions_room_time ON ai_predictions(room_id, prediction_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_ai_predictions_type ON ai_predictions(prediction_type);

-- 2. Anomaly Detections Table
CREATE TABLE IF NOT EXISTS anomaly_detections (
    id VARCHAR(64) PRIMARY KEY,
    room_id VARCHAR(64) NOT NULL,
    anomaly_score DOUBLE PRECISION NOT NULL,
    severity VARCHAR(32) NOT NULL,
    anomaly_detected BOOLEAN NOT NULL DEFAULT FALSE,
    contributing_features TEXT,
    explanation TEXT,
    model_version VARCHAR(64) NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_anomaly_detections_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_anomaly_detections_room_time ON anomaly_detections(room_id, detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_anomaly_detections_severity ON anomaly_detections(severity);
CREATE INDEX IF NOT EXISTS idx_anomaly_detections_detected ON anomaly_detections(anomaly_detected);

-- 3. Risk Assessments Table
CREATE TABLE IF NOT EXISTS risk_assessments (
    id VARCHAR(64) PRIMARY KEY,
    room_id VARCHAR(64) NOT NULL,
    risk_score DOUBLE PRECISION NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    temperature_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    smoke_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    air_quality_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    occupancy_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    anomaly_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    contributing_factors TEXT,
    explanation TEXT,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_risk_assessments_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_risk_assessments_room_time ON risk_assessments(room_id, assessed_at DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assessments_level ON risk_assessments(risk_level);

-- 4. AI Model Metadata Table
CREATE TABLE IF NOT EXISTS ai_model_metadata (
    id VARCHAR(64) PRIMARY KEY,
    model_name VARCHAR(128) NOT NULL,
    model_version VARCHAR(64) NOT NULL,
    model_type VARCHAR(64) NOT NULL,
    training_dataset_size INT NOT NULL DEFAULT 0,
    training_timestamp TIMESTAMP WITH TIME ZONE,
    performance_metrics TEXT,
    model_status VARCHAR(32) NOT NULL DEFAULT 'READY',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_model_name ON ai_model_metadata(model_name);

-- 5. Seed Reference Model Metadata
INSERT INTO ai_model_metadata (
    id, model_name, model_version, model_type, training_dataset_size,
    training_timestamp, performance_metrics, model_status
) VALUES 
(
    'mdl-iso-forest-v1',
    'Isolation Forest Multi-Modal Anomaly Detector',
    'v1.0.0',
    'ISOLATION_FOREST',
    1500,
    CURRENT_TIMESTAMP,
    '{"contamination": 0.05, "n_estimators": 100, "precision": 0.94, "recall": 0.91, "f1_score": 0.925, "anomaly_rate": 0.052}',
    'READY'
),
(
    'mdl-rf-occupancy-v1',
    'Random Forest 15-Minute Occupancy Forecaster',
    'v1.0.0',
    'RANDOM_FOREST_REGRESSOR',
    2400,
    CURRENT_TIMESTAMP,
    '{"n_estimators": 120, "max_depth": 14, "mae": 1.42, "rmse": 2.18, "r2_score": 0.931, "horizon_minutes": 15}',
    'READY'
)
ON CONFLICT (id) DO NOTHING;

-- 6. Seed Initial Baseline AI Assessments for Reference Rooms
INSERT INTO anomaly_detections (
    id, room_id, anomaly_score, severity, anomaly_detected,
    contributing_features, explanation, model_version, detected_at
) VALUES
('anom-init-101', 'rm-101', 0.12, 'NORMAL', FALSE, '{"temperature": 22.4, "humidity": 45.2, "smoke": 0.4, "air_quality": 38.0, "occupancy": 14}', 'All environmental metrics and occupant density within nominal range.', 'v1.0.0', CURRENT_TIMESTAMP),
('anom-init-102', 'rm-102', 0.18, 'NORMAL', FALSE, '{"temperature": 23.1, "humidity": 48.0, "smoke": 0.2, "air_quality": 42.0, "occupancy": 8}', 'Nominal room conditions.', 'v1.0.0', CURRENT_TIMESTAMP),
('anom-init-201', 'rm-201', 0.22, 'NORMAL', FALSE, '{"temperature": 21.8, "humidity": 44.0, "smoke": 0.5, "air_quality": 35.0, "occupancy": 6}', 'Server room climate control optimal.', 'v1.0.0', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO risk_assessments (
    id, room_id, risk_score, risk_level,
    temperature_score, smoke_score, air_quality_score, occupancy_score, anomaly_score,
    contributing_factors, explanation, assessed_at
) VALUES
('risk-init-101', 'rm-101', 14.5, 'NORMAL', 12.0, 4.0, 15.0, 28.0, 12.0, '{"temperature": 22.4, "smoke": 0.4, "air_quality": 38.0, "occupancy": 14}', 'Room operating under safe, optimal environmental conditions.', CURRENT_TIMESTAMP),
('risk-init-102', 'rm-102', 12.8, 'NORMAL', 14.0, 2.0, 16.0, 16.0, 18.0, '{"temperature": 23.1, "smoke": 0.2, "air_quality": 42.0, "occupancy": 8}', 'Room operating under safe, optimal environmental conditions.', CURRENT_TIMESTAMP),
('risk-init-201', 'rm-201', 11.2, 'NORMAL', 10.0, 5.0, 12.0, 20.0, 22.0, '{"temperature": 21.8, "smoke": 0.5, "air_quality": 35.0, "occupancy": 6}', 'Server room operating under safe, optimal environmental conditions.', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ai_predictions (
    id, room_id, prediction_type, predicted_value, prediction_horizon_minutes,
    confidence_or_score, model_version, prediction_timestamp, created_at
) VALUES
('pred-init-101', 'rm-101', 'OCCUPANCY_15MIN', 16.0, 15, 0.94, 'v1.0.0', CURRENT_TIMESTAMP + INTERVAL '15 minutes', CURRENT_TIMESTAMP),
('pred-init-102', 'rm-102', 'OCCUPANCY_15MIN', 9.0, 15, 0.92, 'v1.0.0', CURRENT_TIMESTAMP + INTERVAL '15 minutes', CURRENT_TIMESTAMP),
('pred-init-201', 'rm-201', 'OCCUPANCY_15MIN', 6.0, 15, 0.96, 'v1.0.0', CURRENT_TIMESTAMP + INTERVAL '15 minutes', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
