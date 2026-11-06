# Edge-AI Inference Service

Part of the **Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System**.

## Overview
The AI Engine is an independent FastAPI microservice responsible for ML-based edge intelligence. In future phases, it performs anomaly detection on sensor data and occupancy prediction. In Phase 1, it provides architectural endpoints, runtime information, and an inference communication bridge for the Java backend.

## Architecture
- **Framework**: FastAPI (Python 3.11+)
- **ASGI Server**: Uvicorn
- **Validation**: Pydantic v2
- **ML Tooling**: NumPy, Pandas, Scikit-learn
- **Testing**: Pytest with HTTPX TestClient

## Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Returns `{"status": "UP", "service": "smart-building-ai"}` |
| `GET` | `/info` | Runtime environment, Python version, ML libraries, and model status |
| `POST` | `/api/v1/inference/test` | Test endpoint validating Java-to-Python communication |
| `GET` | `/docs` | Interactive Swagger UI API documentation |
| `GET` | `/redoc` | OpenAPI ReDoc documentation |

## Local Execution

### 1. Create Virtual Environment
```bash
python -m venv venv
# Windows:
.\venv\Scripts\activate
# Linux/macOS:
source venv/bin/activate
```

### 2. Install Dependencies
```bash
pip install -r requirements.txt
```

### 3. Run Service
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. Run Automated Tests
```bash
pytest tests/ -v
```
