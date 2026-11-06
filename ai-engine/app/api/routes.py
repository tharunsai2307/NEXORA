import sys
import numpy as np
import pandas as pd
import sklearn
from fastapi import APIRouter, status

from app.schemas.health import HealthResponse, InfoResponse
from app.schemas.inference import TestInferenceRequest, TestInferenceResponse
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
    logger.debug("Health check requested")
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
    logger.info("System information requested")
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
        phase="Phase 1 - Architectural Foundation"
    )


@router.post(
    "/api/v1/inference/test",
    response_model=TestInferenceResponse,
    status_code=status.HTTP_200_OK,
    summary="Test inference communication",
    description="Validates Java-to-Python HTTP bridge using a validated sensor payload."
)
def post_test_inference(request: TestInferenceRequest) -> TestInferenceResponse:
    return InferenceService.process_test_inference(request)
