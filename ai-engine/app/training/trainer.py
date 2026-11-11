import os
from typing import Dict, Any
from datetime import datetime, timezone

from app.models.anomaly_detector import AnomalyDetector
from app.models.occupancy_predictor import OccupancyPredictor
from app.training.synthetic_data import (
    generate_synthetic_anomaly_dataset,
    generate_synthetic_occupancy_dataset,
)
from app.utils.logger import setup_logger

logger = setup_logger("trainer")


class ModelTrainer:
    """Trains, evaluates, and persists Isolation Forest and Random Forest ML models."""

    def __init__(self, artifacts_dir: str):
        self.artifacts_dir = artifacts_dir

    def train_all(
        self,
        synthetic_samples: int = 2000,
        contamination: float = 0.05,
        save: bool = True
    ) -> Dict[str, Any]:
        """Trains both anomaly detection and occupancy prediction models."""
        logger.info(f"Starting full model training run with {synthetic_samples} samples...")
        start_time = datetime.now(timezone.utc)

        # 1. Train Isolation Forest
        detector = AnomalyDetector(self.artifacts_dir)
        X_anom = generate_synthetic_anomaly_dataset(
            n_samples=synthetic_samples,
            contamination=contamination
        )
        anom_metrics = detector.train(X_anom, save=save)

        # 2. Train Random Forest Occupancy Predictor
        predictor = OccupancyPredictor(self.artifacts_dir)
        X_occ, y_occ = generate_synthetic_occupancy_dataset(
            n_samples=int(synthetic_samples * 1.2)
        )
        occ_metrics = predictor.train(X_occ, y_occ, save=save)

        duration_sec = (datetime.now(timezone.utc) - start_time).total_seconds()
        logger.info(f"Model training completed in {duration_sec:.2f} seconds.")

        return {
            "status": "COMPLETED",
            "duration_seconds": round(duration_sec, 2),
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "models": {
                "isolation_forest": anom_metrics,
                "random_forest_occupancy": occ_metrics,
            }
        }

