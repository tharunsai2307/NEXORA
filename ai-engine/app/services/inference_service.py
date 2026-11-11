from typing import Dict, Any
from app.schemas.inference import TestInferenceRequest, TestInferenceResponse
from app.schemas.anomaly import AnomalyInferenceRequest, AnomalyInferenceResponse
from app.schemas.occupancy import OccupancyPredictionRequest, OccupancyPredictionResponse
from app.schemas.risk import RiskAssessmentRequest, RiskAssessmentResponse
from app.schemas.models import ModelTrainRequest, ModelTrainResponse, ModelStatusResponse
from app.models.registry import model_registry
from app.utils.logger import setup_logger

logger = setup_logger("inference_service")


class InferenceService:

    @staticmethod
    def process_test_inference(request: TestInferenceRequest) -> TestInferenceResponse:
        logger.info(
            f"Received test inference payload from Java backend for sensor: {request.sensor_id}, "
            f"type: {request.reading_type}, value: {request.value}"
        )
        return TestInferenceResponse(
            status="INFERENCE_ECHO_VERIFIED",
            message="Phase 4 Edge AI Intelligence Service Operational.",
            received_payload=request
        )

    @staticmethod
    def detect_anomaly(request: AnomalyInferenceRequest) -> AnomalyInferenceResponse:
        logger.debug(f"Processing anomaly detection for room {request.room_id}")
        return model_registry.anomaly_detector.detect(request)

    @staticmethod
    def predict_occupancy(request: OccupancyPredictionRequest) -> OccupancyPredictionResponse:
        logger.debug(f"Processing occupancy prediction for room {request.room_id}")
        return model_registry.occupancy_predictor.predict(request)

    @staticmethod
    def assess_risk(request: RiskAssessmentRequest) -> RiskAssessmentResponse:
        logger.debug(f"Processing risk assessment for room {request.room_id}")
        return model_registry.risk_engine.assess(request)

    @staticmethod
    def get_model_status() -> ModelStatusResponse:
        metadata = model_registry.get_metadata()
        return ModelStatusResponse(
            models=metadata,
            total_models=len(metadata),
            system_status=model_registry.get_status(),
        )

    @staticmethod
    def train_models(request: ModelTrainRequest) -> ModelTrainResponse:
        logger.info(f"Triggering model training with {request.synthetic_samples} samples...")
        results = model_registry.train_models(
            synthetic_samples=request.synthetic_samples,
            contamination=request.contamination
        )
        return ModelTrainResponse(
            status="SUCCESS",
            message="Models retrained and reloaded into active registry successfully.",
            trained_models=["ISOLATION_FOREST", "RANDOM_FOREST_REGRESSOR"],
            metrics=results["models"],
            timestamp=results["timestamp"]
        )
