from app.schemas.inference import TestInferenceRequest, TestInferenceResponse
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
            message=(
                "Phase 1 communication verified successfully. "
                "Backend-to-AI-service bridge is operational. "
                "Production ML models (anomaly detection & occupancy prediction) will be integrated in Phase 3."
            ),
            received_payload=request
        )
