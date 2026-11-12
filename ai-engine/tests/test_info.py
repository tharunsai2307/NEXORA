from app.api.routes import get_info


def test_info_endpoint():
    data = get_info()
    assert data.service_name == "Smart Building Edge-AI Service"
    assert data.version is not None
    assert data.python_version is not None
    assert "numpy" in data.ml_libraries
    assert "pandas" in data.ml_libraries
    assert "scikit-learn" in data.ml_libraries
    assert "READY" in data.model_status
