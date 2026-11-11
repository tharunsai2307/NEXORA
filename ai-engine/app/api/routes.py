import sys
from typing import List
import numpy as np
import pandas as pd
import sklearn
from fastapi import APIRouter, status

from app.schemas.health import HealthResponse, InfoResponse
from app.schemas.inference import TestInferenceRequest, TestInferenceResponse
from app.schemas.anomaly import AnomalyInferenceRequest, AnomalyInferenceResponse
from app.schemas.occupancy import OccupancyPredictionRequest, OccupancyPredictionResponse
from app.schemas.risk import RiskAssessmentRequest, RiskAssessmentResponse
from app.schemas.models import (
    ModelMetadataDto,
    ModelStatusResponse,
    ModelTrainRequest,
    ModelTrainResponse,
)
from app.services.inference_service import InferenceService
from app.models.registry import model_registry
from app.utils.config import settings
from app.utils.logger import setup_logger

logger = setup_logger("api_routes")
router = APIRouter()


@router.get(
    "/health",
    response_model=HealthResponse,
    status_code=status.HTTP_200_OK,
    summary="Health check endpoint",
    description="Check whether the AI engine is UP and responsive."
)
def get_health() -> HealthResponse:
    return HealthResponse(
        status="UP",
        service="smart-building-ai"
    )


@router.get(
    "/info",
    response_model=InfoResponse,
    status_code=status.HTTP_200_OK,
    summary="System and ML runtime information",
    description="Returns runtime versioning, installed ML packages, and model registry status."
)
def get_info() -> InfoResponse:
    return InfoResponse(
        service_name=settings.SERVICE_NAME,
        version=settings.VERSION,
        python_version=sys.version,
        ml_libraries={
            "numpy": np.__version__,
            "pandas": pd.__version__,
            "scikit-learn": sklearn.__version__,
        },
        model_status=model_registry.get_status(),
        phase="Phase 4 - Edge AI Intelligence"
    )


@router.get(
    "/api/v1/models",
    response_model=List[ModelMetadataDto],
    status_code=status.HTTP_200_OK,
    summary="List all registered AI models",
    description="Returns model architecture, version, performance metrics, and dataset sizes."
)
def get_models() -> List[ModelMetadataDto]:
    return model_registry.get_metadata()


@router.get(
    "/api/v1/models/status",
    response_model=ModelStatusResponse,
    status_code=status.HTTP_200_OK,
    summary="Model registry operational status"
)
def get_models_status() -> ModelStatusResponse:
    return InferenceService.get_model_status()


@router.post(
    "/api/v1/models/train",
    response_model=ModelTrainResponse,
    status_code=status.HTTP_200_OK,
    summary="Trigger model training run"
)
def train_models(request: ModelTrainRequest) -> ModelTrainResponse:
    return InferenceService.train_models(request)


@router.post(
    "/api/v1/models/retrain",
    response_model=ModelTrainResponse,
    status_code=status.HTTP_200_OK,
    summary="Retrain AI models with calibrated dataset"
)
def retrain_models(request: ModelTrainRequest) -> ModelTrainResponse:
    return InferenceService.train_models(request)


@router.post(
    "/api/v1/inference/anomaly",
    response_model=AnomalyInferenceResponse,
    status_code=status.HTTP_200_OK,
    summary="Multi-modal Isolation Forest anomaly detection"
)
def detect_anomaly(request: AnomalyInferenceRequest) -> AnomalyInferenceResponse:
    return InferenceService.detect_anomaly(request)


@router.post(
    "/api/v1/inference/occupancy",
    response_model=OccupancyPredictionResponse,
    status_code=status.HTTP_200_OK,
    summary="Random Forest 15-minute occupancy forecasting"
)
def predict_occupancy(request: OccupancyPredictionRequest) -> OccupancyPredictionResponse:
    return InferenceService.predict_occupancy(request)


@router.post(
    "/api/v1/inference/risk",
    response_model=RiskAssessmentResponse,
    status_code=status.HTTP_200_OK,
    summary="Explainable multi-criteria room safety risk scoring"
)
def assess_risk(request: RiskAssessmentRequest) -> RiskAssessmentResponse:
    return InferenceService.assess_risk(request)


@router.post(
    "/api/v1/inference/test",
    response_model=TestInferenceResponse,
    status_code=status.HTTP_200_OK,
    summary="Test inference communication"
)
def post_test_inference(request: TestInferenceRequest) -> TestInferenceResponse:
    return InferenceService.process_test_inference(request)
