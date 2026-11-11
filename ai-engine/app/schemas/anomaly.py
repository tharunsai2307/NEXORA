from typing import Optional, Dict
from pydantic import BaseModel, Field


class AnomalyInferenceRequest(BaseModel):
    room_id: str = Field(..., description="Unique identifier of room being assessed")
    temperature: Optional[float] = Field(default=22.0, description="Current room temperature in Celsius")
    humidity: Optional[float] = Field(default=45.0, description="Current relative humidity in percent")
    smoke: Optional[float] = Field(default=0.5, description="Simulated smoke concentration in ppm")
    air_quality: Optional[float] = Field(default=40.0, description="Simulated Air Quality Index (0-500)")
    occupancy: Optional[int] = Field(default=10, description="Current room occupant count")
    capacity: int = Field(default=50, description="Maximum permitted room capacity")
    temp_recent_avg: Optional[float] = Field(default=None, description="Recent 15-minute moving average temperature")
    smoke_recent_avg: Optional[float] = Field(default=None, description="Recent 15-minute moving average smoke")
    timestamp: Optional[str] = Field(default=None, description="ISO-8601 observation timestamp")


class AnomalyInferenceResponse(BaseModel):
    room_id: str
    detection_timestamp: str
    anomaly_detected: bool
    anomaly_score: float = Field(..., description="Calibrated anomaly severity score between 0.0 (nominal) and 1.0 (extreme)")
    severity: str = Field(..., description="Classification: NORMAL, LOW, MEDIUM, HIGH, CRITICAL")
    contributing_features: Dict[str, float]
    explanation: str
    model_version: str
