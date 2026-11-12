from app.api.routes import get_health


def test_health_endpoint():
    data = get_health()
    assert data.status == "UP"
    assert data.service == "smart-building-ai"
