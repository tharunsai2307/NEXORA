import pytest
from pydantic import ValidationError

from app.api.routes import (
    post_test_inference,
    detect_anomaly,
    predict_occupancy,
    assess_risk,
    get_models_status
)
from app.schemas.inference import TestInferenceRequest
from app.schemas.anomaly import AnomalyInferenceRequest
from app.schemas.occupancy import OccupancyPredictionRequest
from app.schemas.risk import RiskAssessmentRequest


def test_inference_test_valid_payload():
    payload = TestInferenceRequest(
        sensor_id="temp-sensor-floor-01",
        reading_type="temperature",
        value=23.8,
        unit="CELSIUS",
        metadata={"floor": 1, "room": "104"}
    )
    res = post_test_inference(payload)
    assert res.status == "INFERENCE_ECHO_VERIFIED"
    assert res.received_payload.sensor_id == "temp-sensor-floor-01"
    assert res.received_payload.value == 23.8


def test_inference_test_missing_required_fields():
    with pytest.raises(ValidationError):
        TestInferenceRequest(sensor_id="temp-sensor-floor-01")


def test_anomaly_detection_inference():
    req = AnomalyInferenceRequest(
        room_id="room-101",
        temperature=58.0,
        humidity=25.0,
        smoke=2.5,
        air_quality=160.0,
        occupancy=15,
        capacity=20
    )
    res = detect_anomaly(req)
    assert res.room_id == "room-101"
    assert res.anomaly_score is not None
    assert res.severity in ["NORMAL", "LOW", "MEDIUM", "HIGH", "CRITICAL"]
    assert len(res.contributing_features) > 0


def test_occupancy_prediction_inference():
    req = OccupancyPredictionRequest(
        room_id="room-101",
        current_occupancy=12,
        hour_of_day=14,
        day_of_week=2,
        is_weekend=False,
        room_type="OFFICE",
        max_capacity=20
    )
    res = predict_occupancy(req)
    assert res.room_id == "room-101"
    assert res.predicted_occupancy >= 0
    assert 0.0 <= res.confidence_score <= 1.0


def test_risk_assessment_inference():
    req = RiskAssessmentRequest(
        room_id="room-101",
        temperature=65.0,
        humidity=20.0,
        smoke_detected=True,
        air_quality_index=190.0,
        current_occupancy=15,
        max_capacity=20,
        anomaly_detected=True
    )
    res = assess_risk(req)
    assert res.room_id == "room-101"
    assert res.risk_level in ["LOW", "MODERATE", "ELEVATED", "HIGH", "CRITICAL"]
    assert res.overall_risk_score > 50.0
    assert res.critical_override is True


def test_model_status_registry():
    res = get_models_status()
    assert res.total_models >= 2
    assert "READY" in res.system_status
