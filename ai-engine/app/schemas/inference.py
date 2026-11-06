from datetime import datetime, timezone
from typing import Dict, Any, Optional
from pydantic import BaseModel, Field


class TestInferenceRequest(BaseModel):
    sensor_id: str = Field(..., min_length=1, description="Unique identifier for virtual sensor")
    reading_type: str = Field(..., min_length=1, description="Sensor metric type e.g. temperature, co2, occupancy")
    value: float = Field(..., description="Numeric sensor reading value")
    unit: Optional[str] = Field(default=None, description="Measurement unit e.g. CELSIUS, PPM, COUNT")
    timestamp: Optional[datetime] = Field(
        default_factory=lambda: datetime.now(timezone.utc),
        description="Timestamp of reading"
    )
    metadata: Optional[Dict[str, Any]] = Field(default_factory=dict, description="Additional context attributes")


class TestInferenceResponse(BaseModel):
    status: str = Field(default="INFERENCE_ECHO_VERIFIED", description="Verification status")
    message: str = Field(..., description="Descriptive status message")
    received_payload: TestInferenceRequest = Field(..., description="Echoed and validated input payload")
    echo_timestamp: datetime = Field(
        default_factory=lambda: datetime.now(timezone.utc),
        description="Echo verification timestamp"
    )
    phase: str = Field(default="Phase 1 - Communication Test", description="Current development phase")
    service: str = Field(default="smart-building-ai", description="Service identifier")
