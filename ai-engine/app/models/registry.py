"""Model registry module for AI models.

In Phase 1, this module maintains model lifecycle status and serves as
the architectural placeholder for anomaly detection and occupancy prediction
models to be trained and registered in Phase 3.
"""
from typing import Dict, Any


class ModelRegistry:
    def __init__(self):
        self._models: Dict[str, Any] = {}
        self._status = "READY_PHASE_1_PLACEHOLDER"

    def get_status(self) -> str:
        return self._status

    def is_loaded(self, model_name: str) -> bool:
        return model_name in self._models


model_registry = ModelRegistry()
