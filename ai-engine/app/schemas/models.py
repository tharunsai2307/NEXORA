from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field


class ModelMetadataDto(BaseModel):
    model_name: str
    model_version: str
    model_type: str
    training_dataset_size: int
    training_timestamp: Optional[str]
    performance_metrics: Dict[str, Any]
    model_status: str


class ModelStatusResponse(BaseModel):
    models: List[ModelMetadataDto]
    total_models: int
    system_status: str


class ModelTrainRequest(BaseModel):
    synthetic_samples: int = Field(default=2000, ge=200, le=20000)
    contamination: float = Field(default=0.05, ge=0.01, le=0.25)
    force_retrain: bool = Field(default=False)


class ModelTrainResponse(BaseModel):
    status: str
    message: str
    trained_models: List[str]
    metrics: Dict[str, Any]
    timestamp: str

