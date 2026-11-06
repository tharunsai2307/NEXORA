from typing import Dict, Any
from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str = Field(default="UP", description="Health status of the AI service")
    service: str = Field(default="smart-building-ai", description="Service identifier")


class InfoResponse(BaseModel):
    service_name: str = Field(..., description="Full descriptive name of service")
    version: str = Field(..., description="Service version string")
    python_version: str = Field(..., description="Runtime Python version")
    ml_libraries: Dict[str, str] = Field(..., description="Available ML library versions")
    model_status: str = Field(..., description="State of machine learning models")
    phase: str = Field(default="Phase 1 - Architectural Foundation", description="Project development phase")
