from typing import Optional, List
from pydantic import BaseModel, Field


class OccupancyPredictionRequest(BaseModel):
    room_id: str = Field(..., description="Unique room ID")
    current_occupancy: int = Field(..., ge=0, description="Current recorded occupant count")
    room_type: str = Field(default="OFFICE", description="Room category e.g. OFFICE, CLASSROOM, LAB, CONFERENCE")
    capacity: int = Field(default=50, gt=0, description="Room maximum capacity")
    recent_history: List[int] = Field(default_factory=list, description="Recent occupancy sequence leading up to now")
    time_of_day_hour: Optional[int] = Field(default=None, ge=0, le=23, description="Hour of the day 0-23")
    day_of_week: Optional[int] = Field(default=None, ge=0, le=6, description="Day of week (0=Mon, 6=Sun)")
    prediction_horizon_minutes: int = Field(default=15, ge=1, le=60, description="Forecasting horizon in minutes")


class OccupancyPredictionResponse(BaseModel):
    room_id: str
    current_occupancy: int
    predicted_occupancy: int = Field(..., ge=0, description="Predicted occupant count clamped to [0, capacity]")
    prediction_horizon_minutes: int
    predicted_utilization_pct: float = Field(..., ge=0.0, le=100.0, description="Predicted capacity utilization %")
    prediction_timestamp: str
    model_version: str
    status: str = Field(default="SUCCESS", description="SUCCESS or INSUFFICIENT_DATA")
    confidence_score: float = Field(default=0.92, ge=0.0, le=1.0)

