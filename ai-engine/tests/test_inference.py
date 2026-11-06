from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_inference_test_valid_payload():
    payload = {
        "sensor_id": "temp-sensor-floor-01",
        "reading_type": "temperature",
        "value": 23.8,
        "unit": "CELSIUS",
        "metadata": {
            "floor": 1,
            "room": "104"
        }
    }
    response = client.post("/api/v1/inference/test", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "INFERENCE_ECHO_VERIFIED"
    assert "received_payload" in data
    assert data["received_payload"]["sensor_id"] == "temp-sensor-floor-01"
    assert data["received_payload"]["reading_type"] == "temperature"
    assert data["received_payload"]["value"] == 23.8
    assert data["received_payload"]["unit"] == "CELSIUS"
    assert data["service"] == "smart-building-ai"


def test_inference_test_missing_required_fields():
    # Missing required 'value' and 'reading_type'
    payload = {
        "sensor_id": "temp-sensor-floor-01"
    }
    response = client.post("/api/v1/inference/test", json=payload)
    assert response.status_code == 422
    data = response.json()
    assert data["status"] == "VALIDATION_ERROR"
    assert "details" in data


def test_inference_test_invalid_value_type():
    # Value must be numeric (float)
    payload = {
        "sensor_id": "temp-sensor-floor-01",
        "reading_type": "temperature",
        "value": "not-a-number"
    }
    response = client.post("/api/v1/inference/test", json=payload)
    assert response.status_code == 422
    data = response.json()
    assert data["status"] == "VALIDATION_ERROR"
