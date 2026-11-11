from typing import Optional, Dict
from pydantic import BaseModel, Field


class RiskAssessmentRequest(BaseModel):
    room_id: str
    temperature: Optional[float] = Field(default=22.0, description="Temperature in Celsius")
    humidity: Optional[float] = Field(default=45.0, description="Relative humidity in percent")
    smoke: Optional[float] = Field(default=0.5, description="Smoke concentration in ppm")
    air_quality: Optional[float] = Field(default=40.0, description="Air quality index")
    occupancy: Optional[int] = Field(default=10, description="Current occupant count")
    capacity: int = Field(default=50, description="Room capacity")
    is_anomaly: bool = Field(default=False, description="Flag indicating whether anomaly detection triggered")
    anomaly_score: float = Field(default=0.0, description="Anomaly detector raw/calibrated score")


class RiskAssessmentResponse(BaseModel):
    room_id: str
    overall_risk_score: float = Field(..., ge=0.0, le=100.0, description="Overall explainable risk score (0-100)")
    risk_level: str = Field(..., description="NORMAL (0-29), WARNING (30-59), HIGH_RISK (60-79), CRITICAL (80-100)")
    temperature_score: float = Field(..., ge=0.0, le=100.0)
    smoke_score: float = Field(..., ge=0.0, le=100.0)
    air_quality_score: float = Field(..., ge=0.0, le=100.0)
    occupancy_score: float = Field(..., ge=0.0, le=100.0)
    anomaly_score: float = Field(..., ge=0.0, le=100.0)
    contributing_factors: Dict[str, float]
    explanation: str
    assessment_timestamp: str
    critical_override: bool = Field(default=False, description="True if critical fire/smoke rule overrode the weighted average")
