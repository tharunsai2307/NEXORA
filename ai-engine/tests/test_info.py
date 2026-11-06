from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_info_endpoint():
    response = client.get("/info")
    assert response.status_code == 200
    data = response.json()
    assert data["service_name"] == "Smart Building Edge-AI Service"
    assert "version" in data
    assert "python_version" in data
    assert "ml_libraries" in data
    assert "numpy" in data["ml_libraries"]
    assert "pandas" in data["ml_libraries"]
    assert "scikit-learn" in data["ml_libraries"]
    assert data["model_status"] == "READY_PHASE_1_PLACEHOLDER"
